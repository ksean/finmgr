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
                settledAfterTransaction();
    }

    public boolean currencyIsConsistent() {

        CurrencyUnit currencyUnit = Monetary.getCurrency(currency.getValue());

        return grossAmount.getCurrency().equals(currencyUnit) &&
                commission.getCurrency().equals(currencyUnit) &&
                netAmount.getCurrency().equals(currencyUnit) &&
                returnOnCapital.getCurrency().equals(currencyUnit) &&
                capitalGain.getCurrency().equals(currencyUnit);
    }

    public boolean grossAmountEqualsProductOfQuantityPrice() {

        return grossAmount.isEqualTo(price.multiply(quantity.getValue()).negate());
    }

    public boolean netAmountEqualsGrossMinusCommission() {

        return netAmount.isEqualTo((grossAmount.add(commission)));
    }

    public boolean settledAfterTransaction() {

        return transactionDate.isBefore(settlementDate) || transactionDate.equals(settlementDate);
    }
}
