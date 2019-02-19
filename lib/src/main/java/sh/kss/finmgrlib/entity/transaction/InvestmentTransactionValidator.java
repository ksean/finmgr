/*
    finmgr - A financial transaction framework
    Copyright (C) 2018 Kennedy Software Solutions Inc.

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

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public class InvestmentTransactionValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {

        return InvestmentTransaction.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        InvestmentTransaction transaction = (InvestmentTransaction) o;

        getCurrencyErrors(transaction, errors);

        getSignErrors(transaction, errors);

        getMathErrors(transaction, errors);

        getChronologyErrors(transaction, errors);
    }

    private void getChronologyErrors(InvestmentTransaction transaction, Errors errors) {

        // Settled on or after transaction
        if (transaction.getSettlementDate().isBefore(transaction.getTransactionDate())) {
            errors.rejectValue("settlementDate", "settledBeforeTransaction");
        }
    }

    private void getMathErrors(InvestmentTransaction transaction, Errors errors) {

        // Gross is product of negated quantity and price
        if (!transaction.getGrossAmount().isEqualTo(transaction.getPrice().multiply(transaction.getQuantity().getValue()).negate())) {
            errors.rejectValue("grossAmount", "grossAmountProduct");
        }

        // Net is sum of gross and commission
        if (errors.hasFieldErrors("netAmount") || errors.hasFieldErrors("commission") ||
            !transaction.getNetAmount().isEqualTo(transaction.getGrossAmount().add(transaction.getCommission()))) {

            errors.rejectValue("netAmount", "netAmountSum");
        }
    }

    private void getCurrencyErrors(InvestmentTransaction transaction, Errors errors) {

        // Consistent currency
        CurrencyUnit rootCurrency = Monetary.getCurrency(transaction.getCurrency().getValue());
        if (!transaction.getNetAmount().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("netAmount", "currencyInconsistent");
        }
        if (!transaction.getGrossAmount().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("grossAmount", "currencyInconsistent");
        }
        if (!transaction.getPrice().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("price", "currencyInconsistent");
        }
        if (!transaction.getCommission().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("commission", "currencyInconsistent");
        }
        if (!transaction.getCapitalGain().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("capitalGain", "currencyInconsistent");
        }
        if (!transaction.getReturnOnCapital().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("returnOnCapital", "currencyInconsistent");
        }
    }

    private void getSignErrors(InvestmentTransaction transaction, Errors errors) {

        // Commission always negative or zero
        if (transaction.getCommission().isPositiveOrZero()) {
            errors.rejectValue("commission", "commissionPositiveOrZero");
        }
    }
}
