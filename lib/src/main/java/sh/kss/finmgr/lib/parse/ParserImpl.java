/*
    finmgr - A financial transaction framework
    Copyright (C) 2024 Kennedy Software Solutions Inc.

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
package sh.kss.finmgr.lib.parse;

import org.apache.commons.compress.utils.FileNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Can consume one or many files and convert them into a list of transactions
 */
@Component
public class ParserImpl implements Parser {

    // Log manager
    private final Logger LOG = LoggerFactory.getLogger(ParserImpl.class);

    private CsvFileParser csvFileParser;

    private XlsxFileParser xlsxFileParser;

    private PdfFileParser pdfFileParser;

    /**
     * Traverse the input file for transactions
     *
     * @param file    input file to traverse
     * @return the list of transactions in the file
     */
    private List<InvestmentTransaction> traverseFile(File file) {

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
    public List<InvestmentTransaction> traversePath(String path) {

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
    public List<InvestmentTransaction> traverseFiles(List<File> files) {

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
    public List<InvestmentTransaction> parseFile(File file) {

        LOG.debug(String.format("Parsing input file %s", file.getAbsolutePath()));

        // Lowercase the extension for case-insensitive matching
        String extension = FileNameUtils.getExtension(file.getPath()).toLowerCase();

        LOG.debug(String.format("Found extension %s", extension));

        switch (extension) {

            case "pdf":
                return pdfFileParser.parsePdf(file);

            case "xlsx":
                return xlsxFileParser.parseXlsx(file);

            case "csv":
                return csvFileParser.parseCsv(file);

            default:
                // Don't know how to parse
                return Collections.emptyList();
        }
    }
}
