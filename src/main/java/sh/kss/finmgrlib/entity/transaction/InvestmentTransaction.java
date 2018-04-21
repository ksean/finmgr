package sh.kss.finmgrlib.entity.transaction;

import lombok.Value;
import sh.kss.finmgrlib.entity.*;

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

        //TODO: Iterate all validations

        return true;
    }
}
