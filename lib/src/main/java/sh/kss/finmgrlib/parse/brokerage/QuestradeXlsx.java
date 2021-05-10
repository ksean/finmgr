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
package sh.kss.finmgrlib.parse.brokerage;

import org.apache.poi.ss.usermodel.Row;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.RowParser;

import java.util.Collections;
import java.util.List;

public class QuestradeXlsx extends RowParser {


    /**
     * Checks if the header row from an input file is a match for the parser
     * Ideally this should be implemented in O(1) time
     *
     * @param row Row - the header row from an input file
     * @return the boolean if the input text is a match for the parser
     */
    @Override
    public boolean isMatch(Row row) {
        return false;
    }

    /**
     * Parse the input row and return the transaction found
     *
     * @param row Row - the row from an input file
     * @return the transaction from the row
     */
    @Override
    public InvestmentTransaction parse(Row row) {
        return InvestmentTransaction.builder().build();
    }
}
