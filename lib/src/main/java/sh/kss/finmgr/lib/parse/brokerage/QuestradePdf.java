/*
    finmgr - A financial transaction framework
    Copyright (C) 2024 Kennedy Software Solutions Inc.

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
package sh.kss.finmgr.lib.parse.brokerage;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;
import sh.kss.finmgr.lib.entity.Account;
import sh.kss.finmgr.lib.entity.InvestmentAction;
import sh.kss.finmgr.lib.entity.Quantity;
import sh.kss.finmgr.lib.entity.Security;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgr.lib.parse.PdfParser;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This parser can read updated Questrade PDF documents and parse them for finmgr InvestmentTransactions
 *
 */
@Component
public class QuestradePdf implements PdfParser {

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
    private final Map<String, InvestmentAction> ACTION_MAP = Map.of(
        "buy", InvestmentAction.Buy,
        "sell", InvestmentAction.Sell,
        "deposit", InvestmentAction.Deposit,
        "brw", InvestmentAction.Journal,
        "fee", InvestmentAction.Fee,
        "foreign", InvestmentAction.Exchange,
        "div", InvestmentAction.Distribution,
        "rei", InvestmentAction.Reinvest,
        "nac", InvestmentAction.Corporate
    );


    /**
     * Check if a list of input lines matches known Questrade format
     *
     * @param lines List<String> - the lines of text from an input file
     * @return a boolean if the list is a match
     */
    @Override
    public boolean isMatch(List<String> lines) {

        // Length of the list must be greater than 6 to avoid an out of bounds exception

        return lines.size() > 6 &&
            (
                lines.get(lines.size() - 7).trim().endsWith("Questrade, Inc.")
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

        CurrencyUnit cursorCurrencyUnit = Monetary.getCurrency("CAD");
        Security cursorSecurity = new Security("UNKNOWN", cursorCurrencyUnit);

        List<InvestmentTransaction> transactions = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            cursorSecurity = getSymbol(line, cursorSecurity);

            parseTransaction(cursorCurrencyUnit, Account.UNKNOWN, cursorSecurity, line)
                .ifPresent(transactions::add);

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

            parseTransaction(cursorCurrencyUnit, Account.UNKNOWN, cursorSecurity, multiLine.toString())
                .ifPresent(transactions::add);
        }

        return transactions;
    }


    /**
     *
     *
     * @param currency
     * @param account
     * @param security
     * @param line
     * @return
     */
    private Optional<InvestmentTransaction> parseTransaction(
            CurrencyUnit currency,
            Account account,
            Security security,
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
            .security(security)
            .description(transaction.group("description").trim())
            .price(getMoney(transaction.group("price"), currency))
            .quantity(getQuantity(transaction.group("quantity")))
            .grossAmount(getMoney(transaction.group("gross"), currency))
            .commission(getMoney(transaction.group("commission"), currency))
            .netAmount(getMoney(transaction.group("net"), currency))
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
     * @param currentSecurity
     * @return
     */
    private Security getSymbol(String line, Security currentSecurity) {

        return currentSecurity;
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
