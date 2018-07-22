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
import javax.money.MonetaryAmount;
import java.lang.reflect.Field;
import java.util.Arrays;

public class InvestmentTransactionValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {

        return InvestmentTransaction.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        InvestmentTransaction iv = (InvestmentTransaction) o;

        // Consistent currency
        CurrencyUnit currency = Monetary.getCurrency(iv.getCurrency().getValue());
        if (!iv.getNetAmount().getCurrency().equals(currency)) {
            errors.rejectValue("netAmount", "currencyInconsistent");
        }
        if (!iv.getGrossAmount().getCurrency().equals(currency)) {
            errors.rejectValue("grossAmount", "currencyInconsistent");
        }
        if (!iv.getPrice().getCurrency().equals(currency)) {
            errors.rejectValue("price", "currencyInconsistent");
        }
        if (!iv.getCommission().getCurrency().equals(currency)) {
            errors.rejectValue("commission", "currencyInconsistent");
        }
        if (!iv.getCapitalGain().getCurrency().equals(currency)) {
            errors.rejectValue("capitalGain", "currencyInconsistent");
        }
        if (!iv.getReturnOnCapital().getCurrency().equals(currency)) {
            errors.rejectValue("returnOnCapital", "currencyInconsistent");
        }


        // Commission always negative or zero
        if (iv.getCommission().isPositive()) {
            errors.rejectValue("commission", "commissionPositive");
        }

        // Gross is product of negated quantity and price
        if (!iv.getGrossAmount().getCurrency().equals(iv.getPrice().getCurrency()) ||
                !iv.getGrossAmount().isEqualTo(iv.getPrice().multiply(iv.getQuantity().getValue()).negate())) {
            errors.rejectValue("grossAmount", "grossAmountProduct");
        }

        // Net is sum of gross and commission
        if (!iv.getNetAmount().getCurrency().equals(iv.getGrossAmount().getCurrency()) ||
                !iv.getGrossAmount().getCurrency().equals(iv.getCommission().getCurrency()) ||
                !iv.getNetAmount().isEqualTo(iv.getGrossAmount().add(iv.getCommission()))) {
            errors.rejectValue("netAmount", "netAmountSum");
        }

        // Settled on or after transaction
        if (iv.getSettlementDate().isBefore(iv.getTransactionDate())) {
            errors.rejectValue("settlementDate", "settledBeforeTransaction");
        }
    }
}
