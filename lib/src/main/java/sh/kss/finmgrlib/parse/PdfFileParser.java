/*
    finmgr - A financial transaction framework
    Copyright (C) 2021 Kennedy Software Solutions Inc.

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.brokerage.QuestradePdf;
import sh.kss.finmgrlib.parse.brokerage.QuestradePdfOld;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class provides common processing for .pdf files into finmgr InvestmentTransactions
 *
 */
@Component
public class PdfFileParser {

    // A list of the available PDF parsers
    private static final List<PdfParser> PDF_PARSERS = ImmutableList.of(new QuestradePdfOld(), new QuestradePdf());
    // pdfbox PDF to Text object
    private static PDFTextStripper textStripper;
    // Log manager
    private static final Logger LOG = LoggerFactory.getLogger(PdfFileParser.class);

    private PdfFileParser() {}

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

    public static List<InvestmentTransaction> parsePdf(File file) {

        LOG.debug("Calling parsePdf()");

        if (!canCreateTextStripper()) {

            LOG.error("Could not create the PDF text stripper");

            return Collections.emptyList();
        }

        // .pdf handler
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

                LOG.debug(String.valueOf(stringBuilder));

                // Try all the parsers on the document
                for (PdfParser pdfParser : PDF_PARSERS) {

                    // If it matches then short-circuit and return the parse results
                    if (pdfParser.isMatch(lines)) {

                        LOG.debug(String.format("Matched parser %s", pdfParser.toString()));

                        return pdfParser.parse(lines);

                    } else {

                        LOG.debug(String.format("Did not match parser %s", pdfParser.toString()));
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

        return Collections.emptyList();
    }
}
