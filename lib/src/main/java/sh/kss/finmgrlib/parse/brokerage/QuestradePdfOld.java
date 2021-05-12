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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.javamoney.moneta.Money;
import sh.kss.finmgrlib.entity.Account;
import sh.kss.finmgrlib.entity.InvestmentAction;
import sh.kss.finmgrlib.entity.Quantity;
import sh.kss.finmgrlib.entity.Symbol;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.PdfParser;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can parse old versions of Questrade PDF statements for finmgr InvestmentTransactions
 *
 */
public class QuestradePdfOld implements PdfParser {

    // How Questrade formats their dates
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    // The typical pattern regex for a Questrade transaction
    private final Pattern TRANSACTION_PATTERN = Pattern.compile(
        "(?<transaction>\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<settlement>\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<type>\\w+) "
        + "(?<description>.+) "
        + "(?<quantity>\\(?[\\d|,]+)\\)?\\s[\\- ]*"
        + "(?<price>\\$?[\\d|,]+\\.\\d{3}) "
        + "(?<gross>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?) "
        + "(?<commission>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?|-) "
        + "(?<net>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?)"
    );

    // For multi-line transactions, detect the start
    private final Pattern START_PATTERN = Pattern.compile(
        "(?<transaction>^\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<settlement>\\d{1,2}/\\d{1,2}/\\d{4}) "
        + "(?<type>\\w+)$"
    );

    // For multi-line transactions, detect the end
    private final Pattern END_PATTERN = Pattern.compile(
        "(?<quantity>^\\(?[\\d|,]+)\\)?\\s[\\- ]*"
        + "(?<price>\\$?[\\d|,]+\\.\\d{3}) "
        + "(?<gross>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?) "
        + "(?<commission>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?|-) "
        + "(?<net>\\(?\\$?[\\d|,]+\\.\\d{2}\\)?)$"
    );

    // Another necessary pattern to detect the end of a multi-line transaction
    private final Pattern END_DIV_PATTERN = Pattern.compile("^\\$[\\d|,]+\\.\\d{2}$");

    // Map Questrade action to an internal InvestmentAction
    private final Map<String, InvestmentAction> ACTION_MAP = ImmutableMap.<String, InvestmentAction>builder()
        .put("buy", InvestmentAction.Buy)
        .put("sell", InvestmentAction.Sell)
        .put("deposit", InvestmentAction.Deposit)
        .put("brw", InvestmentAction.Journal)
        .put("fee", InvestmentAction.Fee)
        .put("foreign", InvestmentAction.Exchange)
        .put("div", InvestmentAction.Distribution)
        .put("rei", InvestmentAction.Reinvest)
        .put("nac", InvestmentAction.Corporate)
        .build();


    /**
     * Check if a list of input lines matches known Questrade format
     *
     * @param lines List<String> - the lines of text from an input file
     * @return a boolean if the list is a match
     */
    @Override
    public boolean isMatch(List<String> lines) {

        // Length of the list must be greater than 53 to avoid an out of bounds exception

        return lines.size() > 54 &&
            (
                lines.get(37).trim().endsWith("Questrade")
                || lines.get(38).trim().endsWith("Questrade")
                || lines.get(41).trim().endsWith("Questrade")
                || lines.get(42).trim().endsWith("Questrade")
                || lines.get(44).trim().endsWith("Questrade")
                || lines.get(16).trim().endsWith("Questrade, Inc.")
                || lines.get(36).trim().endsWith("Questrade, Inc.")
                || lines.get(37).trim().endsWith("Questrade, Inc.")
                || lines.get(39).trim().endsWith("Questrade, Inc.")
                || lines.get(41).trim().endsWith("Questrade, Inc.")
                || lines.get(42).trim().endsWith("Questrade, Inc.")
                || lines.get(44).trim().endsWith("Questrade, Inc.")
                || lines.get(48).trim().endsWith("Questrade, Inc.")
                || lines.get(49).trim().endsWith("Questrade, Inc.")
                || lines.get(50).trim().endsWith("Questrade, Inc.")
                || lines.get(54).trim().endsWith("Questrade, Inc.")
            )
        ;
    }

    /**
     *
     *
     * @param lines List<String> - the lines of text from an input file
     * @return
     */
    @Override
    public List<InvestmentTransaction> parse(List<String> lines) {

        CurrencyUnit cursorCurrencyUnit;
        Symbol cursorSymbol = new Symbol("UNKNOWN");

        List<InvestmentTransaction> transactions = Lists.newArrayList();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            cursorCurrencyUnit = Monetary.getCurrency("CAD");
            cursorSymbol = getSymbol(line, cursorSymbol);

            parseTransaction(
                cursorCurrencyUnit,
                Account.UNKNOWN,
                cursorSymbol,
                line
            ).ifPresent(
                transactions::add
            );

            if (! startPartialTransaction(line)) {
                continue;
            }

            StringBuilder multiLine = new StringBuilder();

            multiLine.append(line).append(" ");

            int lastLineIndex = i + 1;

            while (!END_PATTERN.matcher(lines.get(lastLineIndex).trim()).find()
                && !END_DIV_PATTERN.matcher(lines.get(lastLineIndex).trim()).find()
                && lastLineIndex < lines.size() - 1
            ) {

                multiLine.append(lines.get(lastLineIndex)).append(" ");

                lastLineIndex++;
            }

            multiLine.append(lines.get(lastLineIndex));

            parseTransaction(
                cursorCurrencyUnit,
                Account.UNKNOWN,
                cursorSymbol,
                multiLine.toString()
            ).ifPresent(
                transactions::add
            );
        }

        return transactions;
    }


    /**
     *
     *
     * @param currency
     * @param account
     * @param symbol
     * @param line
     * @return
     */
    private Optional<InvestmentTransaction> parseTransaction(
            CurrencyUnit currency,
            Account account,
            Symbol symbol,
            String line
    ) {

        Matcher transaction = TRANSACTION_PATTERN.matcher(line.trim());

        if (! transaction.find()) {

            return Optional.empty();
        }

        InvestmentTransaction investmentTransaction = InvestmentTransaction
            .builder()
            .transactionDate(LocalDate.parse(transaction.group("transaction"), DATE_FORMATTER))
            .settlementDate(LocalDate.parse(transaction.group("settlement"), DATE_FORMATTER))
            .action(getAction(transaction.group("type").trim().toLowerCase()))
            .account(account)
            .currency(currency)
            .symbol(symbol)
            .description(transaction.group("description").trim())
            .price(getMoney(transaction.group("price"), currency))
            .quantity(getQuantity(transaction.group("quantity")))
            .grossAmount(getMoney(transaction.group("gross"), currency))
            .commission(getMoney(transaction.group("commission"), currency))
            .netAmount(getMoney(transaction.group("net"), currency))
            .eligibleDividend(getMoney("0", currency))
            .returnOfCapital(getMoney("0", currency))
            .nonEligibleDividend(getMoney("0", currency))
            .capitalGain(getMoney("0", currency))
            .build();

        return Optional.of(investmentTransaction);
    }

    /**
     *
     *
     * @param line
     * @return
     */
    private boolean startPartialTransaction(String line) {

        return START_PATTERN.matcher(line.trim()).find();
    }

    /**
     *
     *
     * @param line
     * @param currentAccount
     * @return
     */
    private Account getAccount(String line, Account currentAccount) {

        return currentAccount;
    }

    /**
     *
     *
     * @param line
     * @param currentSymbol
     * @return
     */
    private Symbol getSymbol(String line, Symbol currentSymbol) {

        return currentSymbol;
    }

    /**
     *
     *
     * @param action
     * @return
     */
    private InvestmentAction getAction(String action) {

        return ACTION_MAP.getOrDefault(action, InvestmentAction.Other);
    }

    /**
     *
     *
     * @param amount
     * @param currencyUnit
     * @return
     */
    private MonetaryAmount getMoney(String amount, CurrencyUnit currencyUnit) {

        String adjustedAmount = amount
            .replaceAll("-", "0")
            .replaceAll("[^\\d.]", "");

        BigDecimal parsedAmount = new BigDecimal(adjustedAmount);

        return Money.of(parsedAmount, currencyUnit);
    }

    /**
     *
     *
     * @param quantity
     * @return
     */
    private Quantity getQuantity(String quantity) {

        return new Quantity(new BigDecimal(quantity.replaceAll("[^\\d.]", "")));
    }
}
