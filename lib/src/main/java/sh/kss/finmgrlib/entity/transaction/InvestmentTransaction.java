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

import lombok.Value;
import sh.kss.finmgrlib.entity.*;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.time.LocalDate;

@Value
public class InvestmentTransaction {

    LocalDate transactionDate;
    LocalDate settlementDate;
    InvestmentAction action;
    Symbol symbol;
    Quantity quantity;
    MonetaryAmount price;
    MonetaryAmount grossAmount;
    MonetaryAmount commission;
    MonetaryAmount netAmount;
    Account account;
    MonetaryAmount returnOnCapital;
    MonetaryAmount capitalGain;
    Currency currency;

    public boolean isValid() {

        return currencyIsConsistent() &&
                grossAmountEqualsProductOfQuantityPrice() &&
                netAmountEqualsGrossMinusCommission() &&
                commissionNegativeOrZero() &&
                settledOnOrBeforeTransactionDate();
    }

    public boolean currencyIsConsistent() {

        CurrencyUnit currencyUnit = Monetary.getCurrency(currency.getValue());

        return grossAmount.getCurrency().equals(currencyUnit) &&
                commission.getCurrency().equals(currencyUnit) &&
                netAmount.getCurrency().equals(currencyUnit) &&
                returnOnCapital.getCurrency().equals(currencyUnit) &&
                capitalGain.getCurrency().equals(currencyUnit);
    }

    public boolean commissionNegativeOrZero() {

        return commission.isNegativeOrZero();
    }

    public boolean grossAmountEqualsProductOfQuantityPrice() {

        return grossAmount.isEqualTo(price.multiply(quantity.getValue()).negate());
    }

    public boolean netAmountEqualsGrossMinusCommission() {

        return netAmount.isEqualTo((grossAmount.add(commission)));
    }

    public boolean settledOnOrBeforeTransactionDate() {

        return transactionDate.isBefore(settlementDate) || transactionDate.equals(settlementDate);
    }
}
