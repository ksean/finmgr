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

import org.javamoney.moneta.Money;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.CsvParser;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public class RbcCsv implements CsvParser {

    private final String HEADER_MATCH = "\"Date\",\"Activity\",\"Symbol\",\"Symbol Description\",\"Quantity\",\"Price\",\"Settlement Date\",\"Account\",\"Value\",\"Currency\",\"Description\"";

    /**
     * Checks if the header row from an input file is a match for the parser
     * Ideally this should be implemented in O(1) time
     *
     * @param fileInputStream FileInputStream  - the sheet to perform matching against
     * @return the boolean if the input text is a match for the parser
     */
    @Override
    public boolean isMatch(FileInputStream fileInputStream) {

        try (Scanner scanner = new Scanner(fileInputStream, "UTF-8")) {

            int lineCount = 0;
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                lineCount++;

                // RBC header row for data is line 9
                if (lineCount == 9) {

                    if (line.equalsIgnoreCase(HEADER_MATCH)) {

                        return true;

                    } else {

                        break;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Parse the input row and return the transaction found
     *
     * @param line String - the line from a csv file
     * @return the transaction from the row
     */
    @Override
    public InvestmentTransaction parse(String line) {

        String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        if (cols.length != 11) {

            return null;

        } else {

            String currencyCode = cols[9];
            Currency currency;

            switch (currencyCode.toLowerCase()) {

                case "usd":
                    currency = Currency.USD;
                    break;

                default:
                    currency = Currency.CAD;
            }

            return InvestmentTransaction.builder()
                .transactionDate(LocalDate.parse(cols[0]))
                .action(parseAction(cols[1]))
                .symbol(new Symbol(cols[2]))
                .description(cols[10])
                .quantity(new Quantity(new BigDecimal(cols[4])))
                .price(Money.parse(currency.getValue() + " " + cols[5]))
                .settlementDate(LocalDate.parse(cols[6]))
                .account(new Account(cols[7], cols[7], AccountType.NON_REGISTERED))
                .grossAmount(Money.parse(currency.getValue() + " " + cols[8]))
                .netAmount(Money.parse(currency.getValue() + " " + cols[8]))
                .currency(currency)
                // TODO: implemented for intelligent taxation insight
                .commission(Money.of(0, currencyCode))
                .returnOfCapital(Money.of(0, currencyCode))
                .capitalGain(Money.of(0, currencyCode))
                .eligibleDividend(Money.of(0, currencyCode))
                .nonEligibleDividend(Money.of(0, currencyCode))
            .build();
        }
    }

    private InvestmentAction parseAction(String action) {

        // Case insensitive matching
        switch (action.toLowerCase()) {
            case "dividends":
            case "return of capital":
                return InvestmentAction.Distribution;

            case "buy":
                return InvestmentAction.Buy;

            case "deposits & contributions":
                return InvestmentAction.Deposit;

            default:
        }

        return null;
    }
}
