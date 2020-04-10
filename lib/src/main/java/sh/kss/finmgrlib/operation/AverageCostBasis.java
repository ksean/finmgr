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
package sh.kss.finmgrlib.operation;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sh.kss.finmgrlib.entity.Portfolio;
import sh.kss.finmgrlib.entity.Quantity;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.service.TransactionService;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Map;

import static sh.kss.finmgrlib.entity.Quantity.ZERO;

/**
 *
 *
 */
@Component
public class AverageCostBasis extends Operation {

    private final String OPCODE = "ACB";
    private final TransactionService transactionService;

    @Autowired
    public AverageCostBasis(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Portfolio process(Portfolio portfolio, InvestmentTransaction transaction) {

        final CurrencyUnit CURRENCY = transaction.currencyUnit();
        final String TXCODE = transaction.identifier(OPCODE);
        MonetaryAmount acb = Money.of(0, CURRENCY);
        Quantity quantity = ZERO;


        Map<String, Quantity> quantities = portfolio.getQuantities();
        Map<String, MonetaryAmount> monies = portfolio.getMonies();

        if (monies.containsKey(TXCODE)) {
            acb = monies.get(TXCODE);
        }

        if (quantities.containsKey(TXCODE)) {
            quantity = quantities.get(TXCODE);
        }

        switch (transaction.getAction()) {

            // ACB is summed with net amount of purchases
            case Reinvest:
            case Buy:
                quantities.put(TXCODE, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                monies.put(TXCODE, acb.add(transaction.getNetAmount()));

                break;

            // ACB per share remains constant during sales.
            case Sell:
                MonetaryAmount acbPerShare = transactionService.getACB(portfolio, TXCODE);
                quantities.put(TXCODE, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                monies.put(TXCODE, acbPerShare.multiply(quantities.get(TXCODE).getValue().negate()));

                break;

            // Return of Capital reduces ACB
            case Distribution:
                monies.put(TXCODE, acb.subtract(transaction.getReturnOfCapital().multiply(quantity.getValue()).negate()));

                break;

            default:
                break;
        }

        if (quantities.get(TXCODE).getValue().equals(BigDecimal.ZERO)) {

            monies.put(TXCODE, Money.of(0, CURRENCY));
        }

        return portfolio
            .withMonies(monies)
            .withQuantities(quantities);
    }
}
