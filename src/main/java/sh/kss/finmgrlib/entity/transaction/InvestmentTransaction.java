package sh.kss.finmgrlib.entity.transaction;

import lombok.Value;
import sh.kss.finmgrlib.entity.*;

import javax.money.CurrencyUnit;
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

        return currencyIsConsistent();
    }

    public boolean currencyIsConsistent() {

        CurrencyUnit currency = this.price.getCurrency();

        return grossAmount.getCurrency().equals(currency) &&
                commission.getCurrency().equals(currency) &&
                netAmount.getCurrency().equals(currency) &&
                returnOnCapital.getCurrency().equals(currency) &&
                capitalGain.getCurrency().equals(currency);
    }
}
