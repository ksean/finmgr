package sh.kss.finmgrcore;

import lombok.Value;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import java.util.List;


@Value
public class TransactionsResponse {

    List<InvestmentTransaction> transactions;
}
