/*
    finmgr - A financial transaction framework
    Copyright (C) 2019 Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package sh.kss.finmgrlib.parse;

import com.google.common.collect.ImmutableList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.brokerage.Questrade;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PdfParser {

    private static final List<Parser> PARSERS = ImmutableList.of(new Questrade());
    private static PDFTextStripper stripper;

    private static void setUtils() {

        try {
            if (stripper == null) {

                stripper = new PDFTextStripper();
            }
        } catch (IOException ioe) {

            ioe.printStackTrace();
        }
    }


    private static List<InvestmentTransaction> traverse(File file, boolean verbose) {

        if (file.isDirectory()) {

            return fromFiles(file.listFiles(), verbose);
        }

        return fromFile(file, verbose);
    }

    public static List<InvestmentTransaction> fromPath(String path, boolean verbose) {

        try {

            return traverse(new File(path), verbose);
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static List<InvestmentTransaction> fromFiles(File[] files, boolean verbose) {

        List<InvestmentTransaction> transactions = new ArrayList<>();

        try {
            for (File file : files) {

                transactions.addAll(traverse(file, verbose));

            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        return transactions;
    }

    public static List<InvestmentTransaction> fromFile(File file, boolean verbose) {

        setUtils();

        try (PDDocument document = PDDocument.load(file)) {

            if (!document.isEncrypted()) {

                String pdfFileInText = stripper.getText(document);
                List<String> lines = Arrays.asList(pdfFileInText.split("\\r?\\n"));

                if (verbose) {

                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 0; i < lines.size(); i++) {

                        stringBuilder
                            .append(i)
                            .append(": ")
                            .append(lines.get(i))
                            .append("\n");
                    }

                    System.out.println(stringBuilder);
                }

                for (Parser parser : PARSERS) {

                    if (parser.isMatch(lines)) {

                        return parser.parse(lines);
                    }
                }

            }

        }
        catch (IOException ioe) {

            ioe.printStackTrace();
        }

        return Collections.emptyList();
    }
}
