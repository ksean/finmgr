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

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import sh.kss.finmgrlib.entity.InvestmentAction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Map;


/**
 * Validate the state of an investment transaction. Investment transactions must be valid for use in supported
 * transaction operations.
 */
public class InvestmentTransactionValidator implements Validator {


    /**
     *
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(Class<?> aClass) {

        return InvestmentTransaction.class.isAssignableFrom(aClass);
    }

    /**
     *
     * @param o
     * @param errors
     */
    @Override
    public void validate(Object o, Errors errors) {
        // Cast the object into a transaction
        InvestmentTransaction transaction = (InvestmentTransaction) o;

        // Expected string values must not be null or empty
        getNullOrEmptyErrors(transaction, errors);

        // Validate currency is consistent
        getCurrencyErrors(transaction, errors);

        // Validate correct signs are used for credits vs debits
        getSignErrors(transaction, errors);

        // All non-distribution transactions must have the correct quantities and amounts specified
        if (transaction.getAction() != InvestmentAction.Distribution) {

            getMathErrors(transaction, errors);
        }

        // The dates on the transaction must follow chronologically
        getChronologicalErrors(transaction, errors);

        // Can't buy or sell zero quantities
        getZeroFieldValueErrors(transaction, errors);
    }


    /**
     * Check if all of the required string values in the transaction are null or empty
     *
     * @param transaction
     * @param errors
     */
    private void getNullOrEmptyErrors(InvestmentTransaction transaction, Errors errors) {

        Map<String, String> transactionStrings = Map.of(
            "accountAlias", transaction.getAccount().getAlias(),
            "accountId", transaction.getAccount().getId(),
            "currencyValue", transaction.getCurrency().getValue(),
            "symbolValue", transaction.getSymbol().getValue(),
            "description", transaction.getDescription()
        );

        for(Map.Entry<String, String> entry : transactionStrings.entrySet()) {

            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {

                errors.rejectValue(entry.getKey(), "empty");
            }
        }
    }


    /**
     * All required numerical fields during Buy or Sell transactions should be non-zero
     *
     * @param transaction
     * @param errors
     */
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


    /**
     * Settlement date should happen on or after transaction date
     *
     * @param transaction
     * @param errors
     */
    private void getChronologicalErrors(InvestmentTransaction transaction, Errors errors) {

        // Settled on or after transaction
        if (transaction.getSettlementDate().isBefore(transaction.getTransactionDate())) {

            errors.rejectValue("settlementDate", "settledBeforeTransaction");
        }
    }


    /**
     * Gross amount should be the product of quantity and price
     * Net amount should be the sum of gross and commission
     *
     * @param transaction
     * @param errors
     */
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


    /**
     * The same currency should be used for all monetary amounts
     *
     * @param transaction
     * @param errors
     */
    private void getCurrencyErrors(InvestmentTransaction transaction, Errors errors) {

        // Consistent currency
        CurrencyUnit rootCurrency = Monetary.getCurrency(transaction.getCurrency().getValue());

        Map<String, MonetaryAmount> monetaryAmounts = Map.of(
            "netAmount", transaction.getNetAmount(),
            "grossAmount", transaction.getGrossAmount(),
            "price", transaction.getPrice(),
            "commission", transaction.getCommission(),
            "capitalGain", transaction.getCapitalGain(),
            "returnOfCapital", transaction.getReturnOfCapital()
        );

        for (Map.Entry<String, MonetaryAmount> entry : monetaryAmounts.entrySet()) {

            if (!entry.getValue().getCurrency().equals(rootCurrency)) {

                errors.rejectValue(entry.getKey(), "currencyInconsistent");
            }
        }
    }


    /**
     *
     *
     * @param transaction
     * @param errors
     */
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
