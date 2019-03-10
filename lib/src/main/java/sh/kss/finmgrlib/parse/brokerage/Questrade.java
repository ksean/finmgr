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
package sh.kss.finmgrlib.parse.brokerage;

import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.Parser;

import java.util.List;

public class Questrade extends Parser {

    @Override
    public boolean isMatch(List<String> lines) {
        return false;
    }

    @Override
    public List<InvestmentTransaction> parse(List<String> lines) {
        return null;
    }

    public boolean isTransaction(String line) {
        return false;
    }
}
