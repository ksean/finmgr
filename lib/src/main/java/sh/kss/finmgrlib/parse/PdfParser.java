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
import org.apache.pdfbox.text.PDFTextStripperByArea;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.brokerage.Questrade;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PdfParser {

    public static final List<Parser> PARSERS = ImmutableList.of(new Questrade());

    public static List<InvestmentTransaction> fromPath(String path) {

        try {
            File[] files = new File(path).listFiles();

            return fromFiles(files);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        return Collections.emptyList();
    }

    public static List<InvestmentTransaction> fromFiles(File[] files) {

        List<InvestmentTransaction> transactions = new ArrayList<>();

        for (File file : files) {
            transactions.addAll(fromFile(file));
        }

        return transactions;
    }

    public static List<InvestmentTransaction> fromFile(File file) {

        try (PDDocument document = PDDocument.load(file)) {

            if (!document.isEncrypted()) {

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);
                List<String> lines = Arrays.asList(pdfFileInText.split("\\r?\\n"));

                for (Parser parser : PARSERS) {

                    if (parser.isMatch(lines)) {
                        return parser.parse(lines);
                    }
                }

            }

        } catch (IOException ioe) {
            System.out.println(ioe.getLocalizedMessage());
        }

        return Collections.emptyList();
    }

}
