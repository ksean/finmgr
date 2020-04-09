package sh.kss.finmgrlib.service;

import org.springframework.stereotype.Service;
import sh.kss.finmgrlib.entity.Portfolio;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

@Service
public class TransactionService {

    public MonetaryAmount getACB(Portfolio portfolio, String txcode) {

        if (portfolio.getQuantities().get(txcode).getValue().equals(BigDecimal.ZERO)) {
            return portfolio.getMonies().get(txcode);
        }

        return portfolio.getMonies().get(txcode).divide(portfolio.getQuantities().get(txcode).getValue()).negate();
    }
}
