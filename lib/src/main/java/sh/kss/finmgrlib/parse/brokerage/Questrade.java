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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.javamoney.moneta.Money;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.Parser;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Questrade extends Parser {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    private final Currency CAD = new Currency("CAD");
    private final Currency USD = new Currency("USD");

    private final Pattern PATTERN = Pattern.compile("(?<transaction>\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<settlement>\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<type>\\w+) "
        + "(?<description>.+) "
        + "(?<quantity>\\(?[\\d|,]+)\\)?\\s[\\- ]*"
        + "(?<price>\\$?[\\d|,]+\\.\\d{3}) "
        + "(?<gross>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?) "
        + "(?<commission>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?|\\-) "
        + "(?<net>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?)"
    );

    private final Pattern START_PATTERN = Pattern.compile("(?<transaction>^\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<settlement>\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<type>\\w+)$"
    );

    private final Pattern END_PATTERN = Pattern.compile("(?<quantity>^\\(?[\\d|,]+)\\)?\\s[\\- ]*"
        + "(?<price>\\$?[\\d|,]+\\.\\d{3}) "
        + "(?<gross>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?) "
        + "(?<commission>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?|\\-) "
        + "(?<net>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?)$"
    );

    private final Map<String, InvestmentAction> actionMap = ImmutableMap.<String, InvestmentAction>builder()
        .put("buy", InvestmentAction.Buy)
        .put("sell", InvestmentAction.Sell)
        .put("deposit", InvestmentAction.Deposit)
        .put("brw", InvestmentAction.Journal)
        .put("fee", InvestmentAction.Fee)
        .put("foreign", InvestmentAction.Exchange)
        .build();

    @Override
    public boolean isMatch(final List<String> lines) {

        return lines.get(37).trim().endsWith("Questrade")
            || lines.get(38).trim().endsWith("Questrade")
            || lines.get(41).trim().endsWith("Questrade")
            || lines.get(42).trim().endsWith("Questrade")
            || lines.get(44).trim().endsWith("Questrade")
            || lines.get(16).trim().endsWith("Questrade, Inc.");
    }

    @Override
    public List<InvestmentTransaction> parse(final List<String> lines) {

        try {

            Currency cursorCurrency = CAD;
            CurrencyUnit cursorCurrencyUnit;
            Account cursorAccount = new Account("UNKNOWN", "UNKNOWN", AccountType.NON_REGISTERED);
            Symbol cursorSymbol = new Symbol("UNKNOWN");

            List<InvestmentTransaction> transactions = Lists.newArrayList();

            for (int i = 0; i < lines.size(); i++) {

                final String line = lines.get(i);

                cursorCurrency = getCurrency(line, cursorCurrency);
                cursorCurrencyUnit = Monetary.getCurrency(CAD.getValue());
                cursorAccount = getAccount(line, cursorAccount);
                cursorSymbol = getSymbol(line, cursorSymbol);

                parseTransaction(cursorCurrency, cursorCurrencyUnit, cursorAccount, cursorSymbol, line).ifPresent(transactions::add);

                if (startPartialTransaction(line)) {

                    StringBuilder multiLine = new StringBuilder().append(line).append(" ");

                    int lastLineIndex = i + 1;

                    while (!END_PATTERN.matcher(lines.get(lastLineIndex).trim()).find() && lastLineIndex < lines.size()) {

                        multiLine.append(lines.get(lastLineIndex)).append(" ");

                        lastLineIndex++;
                    }

                    multiLine.append(lines.get(lastLineIndex));

                    parseTransaction(cursorCurrency, cursorCurrencyUnit, cursorAccount, cursorSymbol, multiLine.toString()).ifPresent(transactions::add);
                }

            }

            return transactions;
        }
        catch (NullPointerException npe) {

            npe.printStackTrace();
        }

        return Collections.emptyList();
    }

    private Optional<InvestmentTransaction> parseTransaction(final Currency currency,
                                                             final CurrencyUnit currencyUnit,
                                                             final Account account,
                                                             final Symbol symbol,
                                                             final String line) {

        Matcher transaction = PATTERN.matcher(line.trim());

        if (transaction.find()) {

                return Optional.of(
                    InvestmentTransaction.builder()
                        .transactionDate(LocalDate.parse(transaction.group("transaction"), DATE_FORMATTER))
                        .settlementDate(LocalDate.parse(transaction.group("settlement"), DATE_FORMATTER))
                        .action(getAction(transaction.group("type").trim().toLowerCase()))
                        .account(account)
                        .currency(currency)
                        .symbol(symbol)
                        .description(transaction.group("description").trim())
                        .price(getMoney(transaction.group("price"), currencyUnit))
                        .quantity(getQuantity(transaction.group("quantity")))
                        .grossAmount(getMoney(transaction.group("gross"), currencyUnit))
                        .commission(getMoney(transaction.group("commission"), currencyUnit))
                        .netAmount(getMoney(transaction.group("net"), currencyUnit))
                        .build()
                );
        }

        return Optional.empty();
    }

    private boolean startPartialTransaction(final String line) {

        return START_PATTERN.matcher(line.trim()).find();
    }

    private Currency getCurrency(final String line, final Currency currentCurrency) {

        return currentCurrency;
    }

    private Account getAccount(final String line, final Account currentAccount) {

        return currentAccount;
    }

    private Symbol getSymbol(final String line, final Symbol currentSymbol) {

        return currentSymbol;
    }

    private InvestmentAction getAction(final String action) {

        return actionMap.getOrDefault(action, InvestmentAction.Other);
    }

    private MonetaryAmount getMoney(final String amount, final CurrencyUnit currencyUnit) {

        return Money.of(
            new BigDecimal(amount
                .replaceAll("-", "0")
                .replaceAll("[^\\d.]", "")
            ), currencyUnit);
    }

    private Quantity getQuantity(final String quantity) {

        return new Quantity(new BigDecimal(quantity.replaceAll("[^\\d.]", "")));
    }
}
