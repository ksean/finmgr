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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;

import java.util.Optional;

/**
 * A class that can parse the text from a specific format tabular data into finmgr transactions
 */
public interface XlsxParser {

    /**
     * Checks if the header row from an input file is a match for the parser
     * Ideally this should be implemented in O(1) time
     *
     * @param sheet Sheet - the sheet to perform matching against
     * @return the boolean if the input text is a match for the parser
     */
    boolean isMatch(Sheet sheet);

    /**
     * Parse the input row and return the transaction found
     *
     * @param row Row - the row from an input file
     * @return optional transaction from the row
     */
    Optional<InvestmentTransaction> parse(Row row);
}
