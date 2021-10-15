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

import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;

import java.util.List;

/**
 * A class that can parse the text from a specific format of input file into finmgr transactions
 */
public interface PdfParser {

    /**
     * Checks if the input text from an input file is a match for the parser
     * Ideally this should be implemented in O(1) time
     *
     * @param lines List<String> - the lines of text from an input file
     * @return the boolean if the input text is a match for the parser
     */
    boolean isMatch(List<String> lines);

    /**
     * Parse the input text and return all of the transactions found
     * Should produce an empty list if either: there are no transactions in the file
     * or it is parsing an incompatible document (i.e. isMatch produces false)
     *
     * @param lines List<String> - the lines of text from an input file
     * @return the list of transactions from the text
     */
    List<InvestmentTransaction> parse(List<String> lines);
}
