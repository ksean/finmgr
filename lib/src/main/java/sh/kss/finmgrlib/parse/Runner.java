/*
    finmgr - A financial transaction framework
    Copyright (C) 2020 Kennedy Software Solutions Inc.

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.brokerage.QuestradePdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Can consume one or many CSV or PDF files and convert them into a list of transactions
 */
public class Runner {

    // A list of the available PDF parsers
    private static final List<Parser> PARSERS = ImmutableList.of(new QuestradePdf());
    // pdfbox PDF to Text object
    private static PDFTextStripper textStripper;
    // Log manager
    private static final Logger LOG = LogManager.getLogger(Runner.class);

    /**
     * Since creating a PDF Text Stripper can throw an IOException, we should handle it here
     *
     * @return was the creation successful?
     */
    private static boolean canCreateTextStripper() {

        LOG.debug("Calling canCreateTextStripper()");

        try {

            // If the PDF text stripper hasn't been instantiated, do so now
            if (textStripper == null) {

                LOG.debug("PDF text stripper was null - creating object");
                textStripper = new PDFTextStripper();
            }
        } catch (IOException ioe) {

            LOG.error("Failed to create PDF text stripper");
            ioe.printStackTrace();

            return false;
        }

        return true;
    }

    /**
     * Traverse the input file for transactions
     *
     * @param file    input file to traverse
     * @return the list of transactions in the file
     */
    private static List<InvestmentTransaction> traverseFile(File file) {

        LOG.debug(String.format("Traversing file %s", file.getAbsolutePath()));

        if (file.isDirectory()) {

            LOG.debug("File was a directory, traversing files within");

            File[] listFiles = file.listFiles();

            if (listFiles == null) {

                LOG.debug("listFiles was null, returning empty list");

                return Collections.emptyList();
            }

            return traverseFiles(Arrays.asList(listFiles));
        }

        return parseFile(file);
    }

    /**
     * Traverse an input path string for transactions
     *
     * @param path    the absolute path to traverse
     * @return the list of transaction found in the given path
     */
    public static List<InvestmentTransaction> traversePath(String path) {

        LOG.debug(String.format("Traversing path %s", path));

        try {

            // Traverse the file and return the results
            return traverseFile(new File(path));

        }
        catch (NullPointerException npe) {

            LOG.error(String.format("NullPointException when trying to creating a file from the path %s", path));
            npe.printStackTrace();
        }

        // If an exception was thrown then we return an empty list
        return Collections.emptyList();
    }

    /**
     * Traverse a list of input files for transactions
     *
     * @param files   the files to traverse
     * @return the list of transactions found in the files
     */
    public static List<InvestmentTransaction> traverseFiles(List<File> files) {

        LOG.debug(String.format("Traversing input files %s", Arrays.toString(files.toArray())));

        List<InvestmentTransaction> transactions = new ArrayList<>();

        try {

            for (File file : files) {

                transactions.addAll(traverseFile(file));

            }
        } catch (NullPointerException npe) {

            LOG.error(String.format("NullPointException when trying to creating a file from the path, %s", npe.getMessage()));
            npe.printStackTrace();
        }

        return transactions;
    }

    /**
     * Parse the specified file for transactions
     *
     * @param file    the file to parse
     * @return the list of transactions parsed
     */
    public static List<InvestmentTransaction> parseFile(File file) {

        LOG.info(String.format("Parsing input file %s", file.getAbsolutePath()));

        if (!canCreateTextStripper()) {

            LOG.error("Could not create the PDF text stripper");

            return Collections.emptyList();
        }

        // Try to load the file in PDDocument
        try (PDDocument document = PDDocument.load(file)) {

            // Skip any encrypted documents
            if (!document.isEncrypted()) {

                // Get the text from the document
                String pdfFileInText = textStripper.getText(document);

                // Split it into a list of strings
                List<String> lines = Arrays.asList(pdfFileInText.split("\\r?\\n"));

                // In debug mode output every line in the document
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < lines.size(); i++) {

                    stringBuilder
                        .append(i)
                        .append(": ")
                        .append(lines.get(i))
                        .append("\n");
                }

                LOG.debug(stringBuilder);

                // Try all the parsers on the document
                for (Parser parser : PARSERS) {

                    // If it matches then short-circuit and return the parse results
                    if (parser.isMatch(lines)) {

                        LOG.debug(String.format("Matched parser %s", parser.toString()));

                        return parser.parse(lines);

                    } else {

                        LOG.debug(String.format("Did not match parser %s", parser.toString()));
                    }
                }
            } else {

                LOG.debug(String.format("Skipped file %s because it is encrypted", file.getAbsoluteFile()));
            }
        }
        catch (IOException ioe) {

            LOG.error(String.format("IOException occurred when loading PDDocumnent with file %s", file.getAbsoluteFile()));
            ioe.printStackTrace();
        }

        // IOException, encrypted files, or a file that doesn't match will all return an empty list
        return Collections.emptyList();
    }
}
