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
package sh.kss.finmgr.lib.parse;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgr.lib.parse.brokerage.RbcCsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * This class provides common functionality for the processing of .csv files into finmgr InvestmentTransactions
 *
 */
@Component
public class CsvFileParserImpl implements CsvFileParser {
    // Log manager
    private final Logger LOG = LoggerFactory.getLogger(CsvFileParserImpl.class);
    // A list of the available Csv parsers
    private final List<CsvParser> CSV_PARSERS = ImmutableList.of(new RbcCsv());

    @Override
    public List<InvestmentTransaction> parseCsv(File file) {

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

    private List<InvestmentTransaction> parseLines(File file, CsvParser csvParser) {

        LOG.debug("Calling parseLines()");

        // Wrap in try catch due to opening file input stream
        try (FileInputStream inputStream = new FileInputStream(file)) {

            // Instantiate the list to hold transactions
            List<InvestmentTransaction> transactions = new ArrayList<>();

            // Create a UTF-8 Scanner on the input stream
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);

            // While more lines exist
            while (scanner.hasNextLine()) {

                // Fetch the line
                String line = scanner.nextLine();

                LOG.debug(line);

                // Parse the line and add to the list if a valid row was found
                csvParser.parse(line)
                    .ifPresent(transactions::add);
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
