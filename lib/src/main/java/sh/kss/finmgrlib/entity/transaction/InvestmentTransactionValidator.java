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
package sh.kss.finmgrlib.entity.transaction;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import sh.kss.finmgrlib.entity.InvestmentAction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;

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

        if (transaction.getAction() != InvestmentAction.Distribution) {
            getMathErrors(transaction, errors);
        }

        getChronologyErrors(transaction, errors);

        getZeroFieldValueErrors(transaction, errors);
    }

    private void getZeroFieldValueErrors(InvestmentTransaction transaction, Errors errors) {
        switch (transaction.getAction()) {

            case Buy:
                if (transaction.getNetAmount().isZero()) {
                    errors.rejectValue("netAmount", "netAmountZero");
                }

                if (transaction.getQuantity().getValue().equals(BigDecimal.ZERO)) {
                    errors.rejectValue("quantity", "quantityZero");
                }

                if (transaction.getPrice().isNegativeOrZero()) {
                    errors.rejectValue("price", "priceNegativeOrZero");
                }

                if (transaction.getGrossAmount().isZero()) {
                    errors.rejectValue("quantity", "quantityZero");
                }

                break;

            case Sell:
                if (transaction.getQuantity().getValue().equals(BigDecimal.ZERO)) {
                    errors.rejectValue("quantity", "quantityZero");
                }

                if (transaction.getPrice().isNegativeOrZero()) {
                    errors.rejectValue("price", "priceNegativeOrZero");
                }

                if (transaction.getGrossAmount().isZero()) {
                    errors.rejectValue("quantity", "quantityZero");
                }

                break;

            case Distribution:
            default:
                break;
        }
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

        if (!transaction.getReturnOfCapital().getCurrency().equals(rootCurrency)) {
            errors.rejectValue("returnOnCapital", "currencyInconsistent");
        }
    }

    private void getSignErrors(InvestmentTransaction transaction, Errors errors) {

        // Commission always negative or zero
        if (transaction.getCommission().isPositive()) {
            errors.rejectValue("commission", "commissionPositive");
        }

        // Return of capital always positive or zero
        if (transaction.getReturnOfCapital().isNegative()) {
            errors.rejectValue("returnOfCapital", "returnOfCapitalNegative");
        }

        // Capital gain always positive or zero
        if (transaction.getCapitalGain().isNegative()) {
            errors.rejectValue("capitalGain", "capitalGainNegative");
        }

        /* SELL
           Quantity must be negative, gross must be positive
         */
        if (transaction.getAction().equals(InvestmentAction.Sell)) {

            if (transaction.getQuantity().getValue().compareTo(BigDecimal.ZERO) > 0) {
                errors.rejectValue("quantity", "sellQuantityPositive");
            }

            if (transaction.getGrossAmount().isNegativeOrZero()) {
                errors.rejectValue("grossAmount", "sellGrossAmountNegativeOrZero");
            }
        }

        /* BUY
           Quantity must be positive, gross must be negative
         */
        if (transaction.getAction().equals(InvestmentAction.Buy)) {

            if (transaction.getQuantity().getValue().compareTo(BigDecimal.ZERO) < 0) {
                errors.rejectValue("quantity", "buyQuantityNegative");
            }

            if (transaction.getGrossAmount().isPositiveOrZero()) {
                errors.rejectValue("grossAmount", "buyGrossAmountPositiveOrZero");
            }
        }
    }
}
