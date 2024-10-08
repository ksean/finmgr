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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;
import sh.kss.finmgr.lib.entity.*;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgr.lib.parse.XlsxParser;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * This parser can parse Questrade .xlsx Excel file exports for finmgr InvestmentTransactions
 *
 */
@Component
public class QuestradeXlsx implements XlsxParser {

    public final String[] HEADER_COLUMNS = {
        "Transaction Date",
        "Settlement Date",
        "Action",
        "Symbol",
        "Description",
        "Quantity",
        "Price",
        "Gross Amount",
        "Commission",
        "Net Amount",
        "Currency",
        "Account #",
        "Activity Type",
        "Account Type",
    };

    /**
     * Checks if the header row from an input file is a match for the parser
     * Ideally this should be implemented in O(1) time
     *
     * @param sheet Sheet - the sheet to perform matching against
     * @return the boolean if the input text is a match for the parser
     */
    @Override
    public boolean isMatch(Sheet sheet) {

        Row header = sheet.getRow(0);

        for (int i = 0; i < HEADER_COLUMNS.length; i++) {

            // Order and column header strings must all match
            if (!HEADER_COLUMNS[i].equals(header.getCell(i).getStringCellValue())) {

                return false;
            }
        }

        // Also check if column length matches to ensure blank rows don't match
        return header.getLastCellNum() - header.getFirstCellNum() == HEADER_COLUMNS.length;
    }

    /**
     * Parse the input row and return the transaction found
     *
     * @param row Row - the row from an input file
     * @return the transaction from the row
     */
    @Override
    public Optional<InvestmentTransaction> parse(Row row) {

        String currencyCode = row.getCell(10).getStringCellValue();
        CurrencyUnit currency = Monetary.getCurrency(currencyCode);

        return Optional.of(InvestmentTransaction.builder()
            .transactionDate(LocalDate.parse(row.getCell(0).getStringCellValue().substring(0, 10)))
            .settlementDate(LocalDate.parse(row.getCell(1).getStringCellValue().substring(0, 10)))
            .action(Objects.requireNonNull(parseAction(row.getCell(2), row.getCell(12))))
            .security(new Security(row.getCell(3).getStringCellValue(), currency))
            .description(row.getCell(4).getStringCellValue())
            .quantity(new Quantity(new BigDecimal(row.getCell(5).getStringCellValue())))
            .price(Money.parse(currency.getCurrencyCode() + " " + row.getCell(6).getStringCellValue()))
            .grossAmount(Money.parse(currency.getCurrencyCode() + " " + row.getCell(7).getStringCellValue()))
            .commission(Money.parse(currency.getCurrencyCode() + " " + row.getCell(8).getStringCellValue()))
            .netAmount(Money.parse(currency.getCurrencyCode() + " " + row.getCell(9).getStringCellValue()))
            .currency(currency)
            .account(new Account(row.getCell(11).getStringCellValue(), row.getCell(11).getStringCellValue(), parseAccountType(row.getCell(13))))
        .build());
    }

    private InvestmentAction parseAction(Cell cell, Cell descriptor) {

        // Case insensitive matching
        switch (cell.getStringCellValue().toLowerCase()) {
            case "con":
                return InvestmentAction.Deposit;

            case "rei":
                return InvestmentAction.Reinvest;

            case "buy":
                return InvestmentAction.Buy;

            case "sell":
                return InvestmentAction.Sell;

            case "div":
            case "dep":
                return InvestmentAction.Distribution;

            case "eft":
            case "wdl":
            case "wdr":
                return InvestmentAction.Withdrawal;

            case "fxt":
                return InvestmentAction.Exchange;

            case "hst":
            case "fch":
                return InvestmentAction.Fee;

            case "brw":
                return InvestmentAction.Journal;

            case "dis":
            case "nac":
                return InvestmentAction.Corporate;

            case "":
                if (descriptor.getStringCellValue().equalsIgnoreCase("interest")) {

                    return InvestmentAction.Fee;

                } else {

                    return InvestmentAction.Distribution;
                }

            default:
        }

        return null;
    }

    private AccountType parseAccountType(Cell cell) {

        switch (cell.getStringCellValue().toLowerCase()) {

            case "individual tfsa":
                return AccountType.TFSA;

            case "individual rrsp":
                return AccountType.RRSP;

            case "individual margin":
                return AccountType.NON_REGISTERED;

            default:
        }

        return null;
    }
}
