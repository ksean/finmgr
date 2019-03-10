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

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Questrade extends Parser {

    @Override
    public boolean isMatch(List<String> lines) {

        return lines.get(37).trim().endsWith("Questrade");
    }

    @Override
    public List<InvestmentTransaction> parse(List<String> lines) {

        try {
            CurrencyUnit currency = Monetary.getCurrency("CAD");

            for (String line : lines) {

                Pattern transactionPattern = Pattern.compile("(?<transaction>\\d{1,2}/\\d{1,2}/\\d{4}) "
                + "(?<settlement>\\d{1,2}/\\d{1,2}/\\d{4}) "
                + "(?<type>\\w+) "
                + "(?<description>.+) "
                + "(?<quantity>[\\d|,]+) "
                + "(?<price>\\$[\\d|,]+\\.\\d{3}) "
                + "(?<gross>\\(?\\$[\\d|,]+\\.\\d{2}\\)?) "
                + "(?<commission>\\(?\\$[\\d|,]+\\.\\d{2}\\)?) "
                + "(?<net>\\(?\\$[\\d|,]+\\.\\d{2}\\)?)");

                Matcher transaction = transactionPattern.matcher(line.trim());

                if (transaction.find()) {

                    System.out.println("transaction: " + transaction.group("transaction"));
                    System.out.println("settlement: " + transaction.group("settlement"));
                    System.out.println("type: " + transaction.group("type"));
                    System.out.println("description: " + transaction.group("description"));
                    System.out.println("quantity: " + transaction.group("quantity"));
                    System.out.println("price: " + transaction.group("price"));
                    System.out.println("gross: " + transaction.group("gross"));
                    System.out.println("commission: " + transaction.group("commission"));
                    System.out.println("net: " + transaction.group("net"));
                }
            }
        } catch (NullPointerException npe) {

            System.out.println(npe.getLocalizedMessage());
        }

        return Collections.emptyList();
    }
}
