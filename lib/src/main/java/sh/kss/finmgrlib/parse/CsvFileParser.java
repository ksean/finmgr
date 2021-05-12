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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.brokerage.RbcCsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * This class provides common functionality for the processing of .csv files into finmgr InvestmentTransactions
 *
 */
public class CsvFileParser {
    // Log manager
    private static final Logger LOG = LoggerFactory.getLogger(CsvFileParser.class);
    // A list of the available Csv parsers
    private static final List<CsvParser> CSV_PARSERS = ImmutableList.of(new RbcCsv());

    public static List<InvestmentTransaction> parseCsv(File file) {

        LOG.debug("Calling parseCsv()");

        // Wrap in try catch due to opening file input stream
        try (FileInputStream inputStream = new FileInputStream(file)) {

            // Try to match against known row parsers
            for (CsvParser csvParser : CSV_PARSERS) {

                LOG.debug(String.format("Check CsvParser %s", csvParser));

                // If the header matches, parse it
                if (csvParser.isMatch(inputStream)) {

                    LOG.debug(String.format("Matched CsvParser %s", csvParser));

                    return parseLines(file, csvParser);
                }
            }

            // If no Row Parsers matched, the file format is unknown and no transactions are parsed
            return Collections.emptyList();


        } catch (FileNotFoundException fnfe) {

            LOG.error(String.format("FileNotFoundException occurred when creating FileInputStream for file %s", file.getAbsoluteFile()));
            fnfe.printStackTrace();

        } catch (IOException ioe) {

            LOG.error(String.format("IOException occurred when creating FileInputStream for file %s", file.getAbsoluteFile()));
            ioe.printStackTrace();
        }

        return Collections.emptyList();
    }

    private static List<InvestmentTransaction> parseLines(File file, CsvParser csvParser) {

        LOG.debug("Calling parseLines()");

        // Wrap in try catch due to opening file input stream
        try (FileInputStream inputStream = new FileInputStream(file)) {

            List<InvestmentTransaction> transactions = new ArrayList<>();
            Scanner scanner = new Scanner(inputStream, "UTF-8");

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();

                LOG.debug(line);

                InvestmentTransaction parsedTransaction = csvParser.parse(line);

                if (parsedTransaction != null) {

                    transactions.add(parsedTransaction);
                }
            }

            return transactions;

        } catch (FileNotFoundException fnfe) {

            LOG.error(String.format("FileNotFoundException occurred when creating FileInputStream for file %s", file.getAbsoluteFile()));
            fnfe.printStackTrace();

        } catch (IOException ioe) {

            LOG.error(String.format("IOException occurred when creating FileInputStream for file %s", file.getAbsoluteFile()));
            ioe.printStackTrace();
        }

        return Collections.emptyList();
    }
}
