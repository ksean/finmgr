package sh.kss.finmgrlib;

import org.javamoney.moneta.Money;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.List;

public class Operations {

    public static MonetaryAmount getAcbPerShare(List<InvestmentTransaction> transactions) {

        // TODO: Implement base currency + auto conversion
        CurrencyUnit currency = Monetary.getCurrency(transactions.get(0).getCurrency().getValue());

        MonetaryAmount acb = Money.of(0, currency);
        BigDecimal quantity = new BigDecimal("0");
        final BigDecimal ZERO = new BigDecimal("0");

        for (InvestmentTransaction transaction : transactions) {

            switch (transaction.getAction()) {

                // ACB is summed with net amount of purchases
                case Reinvest:
                case Buy:
                    acb = acb.add(transaction.getNetAmount());
                    quantity = transaction.getQuantity().getValue().add(quantity);

                    break;

                // ACB per share remains constant during sales.
                case Sell:
                    MonetaryAmount acbPerShare = acb.divide(quantity);
                    quantity = transaction.getQuantity().getValue().add(quantity);

                    acb = acbPerShare.multiply(quantity);

                    break;

                // Return of Capital reduces ACB. Capital Gains distribution increases ACB
                case Distribution:
                    MonetaryAmount addend = transaction.getCapitalGain().multiply(transaction.getQuantity().getValue());
                    MonetaryAmount subtrahend = transaction.getReturnOnCapital().multiply(transaction.getQuantity().getValue());

                    acb = acb.add(addend).subtract(subtrahend);

                    break;

                default:
                    break;
            }

            if (quantity.equals(ZERO)) {

                acb = Money.of(0, currency);
            }
        }

        if (quantity.equals(ZERO)) {

            return acb;
        }

        return acb.divide(quantity);
    }
}
