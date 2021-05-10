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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.brokerage.QuestradeXlsx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class XlsxParser {
    // Log manager
    private static final Logger LOG = LogManager.getLogger(XlsxParser.class);
    // A list of the available Xlsx parsers
    private static final List<RowParser> ROW_PARSERS = ImmutableList.of(new QuestradeXlsx());

    public static List<InvestmentTransaction> parseXlsx(File file) {

        LOG.debug("Calling parseXlsx()");

        // Wrap in try catch due to opening file input stream
        try {

            // Get the root sheet and header row
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());

            // Try to match against known row parsers
            for (RowParser rowParser : ROW_PARSERS) {

                LOG.debug(String.format("Check RowParser %s", rowParser));

                // If the header matches, parse it
                if (rowParser.isMatch(header)) {

                    LOG.debug(String.format("Matched RowParser %s", rowParser));

                    Iterator<Row> rowIterator = sheet.rowIterator();
                    List<InvestmentTransaction> transactions = new ArrayList<>();

                    boolean skippedHeader = false;
                    // Parse each row into an InvestmentTransaction
                    while (rowIterator.hasNext()) {

                        if (!skippedHeader) {
                            rowIterator.next();
                            skippedHeader = true;
                        }

                        transactions.add(rowParser.parse(rowIterator.next()));
                    }

                    return transactions;
                }
            }

            // If no Row Parsers matched, the file format is unknown and no transactions are parsed
            return Collections.emptyList();


        } catch (FileNotFoundException fnfe) {

            LOG.error(String.format("FileNotFoundException occurred when creating FileInputStream for file %s", file.getAbsoluteFile()));
            fnfe.printStackTrace();

        }  catch (IOException ioe) {

            LOG.error(String.format("IOException occurred when creating FileInputStream for file %s", file.getAbsoluteFile()));
            ioe.printStackTrace();
        }

        return Collections.emptyList();
    }
}
