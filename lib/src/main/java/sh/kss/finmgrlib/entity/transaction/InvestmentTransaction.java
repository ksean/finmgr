/*
    finmgr - A financial transaction framework
    Copyright (C) 2020 Kennedy Software Solutions Inc.

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
package sh.kss.finmgrlib.entity.transaction;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import sh.kss.finmgrlib.entity.*;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.time.LocalDate;

/**
 *
 *
 */
@Value
@With
@Builder(toBuilder = true)
public class InvestmentTransaction implements Comparable<InvestmentTransaction> {

    // What, where, when this transaction happened
    @NonNull LocalDate transactionDate;
    @NonNull LocalDate settlementDate;
    @NonNull InvestmentAction action;
    @NonNull Account account;
    @NonNull Currency currency;
    Symbol symbol;
    @NonNull String description;

    // Details of transactions that change the quantity of the security
    @NonNull MonetaryAmount price;
    @NonNull Quantity quantity;
    @NonNull MonetaryAmount grossAmount;
    @NonNull MonetaryAmount commission;
    @NonNull MonetaryAmount netAmount;

    // Most frequently referenced distribution
    @NonNull MonetaryAmount returnOfCapital;
    @NonNull MonetaryAmount capitalGain;
    @NonNull MonetaryAmount eligibleDividend;
    @NonNull MonetaryAmount nonEligibleDividend;

    MonetaryAmount foreignBusinessIncome;
    MonetaryAmount foreignNonBusinessIncome;
    MonetaryAmount otherIncome;
    MonetaryAmount nonReportableDistribution;
    MonetaryAmount capitalGainsDeductionEligible;
    MonetaryAmount foreignBusinessIncomeTaxPaid;
    MonetaryAmount foreignNonBusinessIncomeTaxPaid;


    public String identifier(String opcode) {

        return account.getAccountType()
            + "-"
            + symbol.getValue()
            + "-"
            + opcode;
    }


    public CurrencyUnit currencyUnit() {

        return Monetary.getCurrency(currency.getValue());
    }


    @Override
    public int compareTo(InvestmentTransaction transaction) {

        return getTransactionDate().compareTo(transaction.getTransactionDate());
    }
}
