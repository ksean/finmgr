package sh.kss.finmgrlib.entity.transaction;

import lombok.Value;
import sh.kss.finmgrlib.entity.Account;
import sh.kss.finmgrlib.entity.TransactionCategory;

import javax.money.MonetaryAmount;
import java.time.LocalDate;

@Value
public class PersonalTransaction {
    LocalDate transactionDate;
    LocalDate settlementDate;
    MonetaryAmount amount;
    Account account;
    String description;
    TransactionCategory transactionCategory;

    public boolean isValid() {

        //TODO: Iterate all validations

        return true;
    }
}
