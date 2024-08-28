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
package sh.kss.finmgr.lib.entity.transaction;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import sh.kss.finmgr.lib.entity.Account;
import sh.kss.finmgr.lib.entity.InvestmentAction;
import sh.kss.finmgr.lib.entity.Quantity;
import sh.kss.finmgr.lib.entity.Security;

import jakarta.annotation.Nullable;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.time.LocalDate;

/**
 * An investment transaction is a transaction that is executed within an investment/brokerage account
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
    @NonNull CurrencyUnit currency;
    @Nullable Security security;
    @NonNull String description;

    // Details of transactions that change the quantity of the security
    @Nullable MonetaryAmount price;
    @Nullable Quantity quantity;
    @Nullable MonetaryAmount grossAmount;
    @Nullable MonetaryAmount commission;
    @NonNull MonetaryAmount netAmount;

    // Most frequently referenced distribution
    @Nullable MonetaryAmount returnOfCapital;
    @Nullable MonetaryAmount capitalGain;
    @Nullable MonetaryAmount eligibleDividend;
    @Nullable MonetaryAmount nonEligibleDividend;

    @Nullable MonetaryAmount foreignBusinessIncome;
    @Nullable MonetaryAmount foreignNonBusinessIncome;
    @Nullable MonetaryAmount otherIncome;
    @Nullable MonetaryAmount nonReportableDistribution;
    @Nullable MonetaryAmount capitalGainsDeductionEligible;
    @Nullable MonetaryAmount foreignBusinessIncomeTaxPaid;
    @Nullable MonetaryAmount foreignNonBusinessIncomeTaxPaid;


    public String identifier(String opcode, String symbol) {

        return account.getAccountType()
            + "-"
            + symbol
            + "-"
            + opcode;
    }


    @Override
    public int compareTo(InvestmentTransaction transaction) {

        return getTransactionDate().compareTo(transaction.getTransactionDate());
    }
}
