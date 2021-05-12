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

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RbcCsv implements CsvParser {

    // Match RBC style header from raw csv
    private final String HEADER_MATCH = "\"Date\",\"Activity\",\"Symbol\",\"Symbol Description\",\"Quantity\",\"Price\",\"Settlement Date\",\"Account\",\"Value\",\"Currency\",\"Description\"";
    // RBC style dates in csv file
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, u");

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

        // Split line into raw string array
        String[] raw_cols = line.split("\",\"");

        // Clean the columns for object population
        List<String> cols = Arrays.stream(raw_cols).map(x -> x.replaceAll("^\"|\"$", "")).collect(Collectors.toList());

        // Must have 11 columns, and the 8th column must contain an 8 digit account number
        if (cols.size() != 11 || cols.get(7).length() != 8) {

            return null;

        } else {

            String currencyCode = cols.get(9);
            Money zero = Money.of(0, currencyCode);
            CurrencyUnit currency = Monetary.getCurrency(currencyCode);

            // Quantity defaults to 0 if blank
            Quantity quantity;

            if (cols.get(4).length() == 0) {

                quantity = new Quantity(BigDecimal.ZERO);

            } else {

                quantity = new Quantity(new BigDecimal(cols.get(4)));
            }

            // Price defaults to 0 if blank
            Money price;

            if (cols.get(5).length() == 0) {

                price = zero;

            } else {

                price = Money.parse(currency.getCurrencyCode() + " " + cols.get(5));
            }

            return InvestmentTransaction.builder()
                .transactionDate(LocalDate.parse(cols.get(0), DATE_FORMATTER))
                .action(parseAction(cols.get(1)))
                .symbol(new Symbol(cols.get(2)))
                .description(cols.get(10))
                .quantity(quantity)
                .price(price)
                .settlementDate(LocalDate.parse(cols.get(6), DATE_FORMATTER))
                .account(new Account(cols.get(7), cols.get(7), AccountType.NON_REGISTERED))
                .grossAmount(Money.parse(currency.getCurrencyCode() + " " + cols.get(8)))
                .netAmount(Money.parse(currency.getCurrencyCode() + " " + cols.get(8)))
                .currency(currency)
                // TODO: implemented for intelligent taxation insight
                .commission(zero)
                .returnOfCapital(zero)
                .capitalGain(zero)
                .eligibleDividend(zero)
                .nonEligibleDividend(zero)
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
