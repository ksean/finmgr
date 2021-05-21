/*
    finmgr - A financial transaction framework
    Copyright (C) 2021 Kennedy Software Solutions Inc.

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
 * Average cost basis will enhance a portfolio with data necessary to calculate each holdings ACB, given an input
 * investment transaction
 *
 */
@Component
public class AverageCostBasis extends Operation {

    private final TransactionService transactionService;

    @Autowired
    public AverageCostBasis(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Portfolio process(Portfolio portfolio, InvestmentTransaction transaction) {

        // ACB changes are limited to specific symbols so short circuit if not present
        if (transaction.getSymbol() == null) {
            return portfolio;
        }

        // Unique code to this operation
        final String OPERATION_CODE = "ACB";
        final CurrencyUnit CURRENCY = transaction.getCurrency();
        final String TRANSACTION_CODE = transaction.identifier(OPERATION_CODE, transaction.getSymbol().getValue());
        MonetaryAmount costBasis = Money.of(0, CURRENCY);
        Quantity quantity = ZERO;


        Map<String, Quantity> quantityMap = portfolio.getQuantities();
        Map<String, MonetaryAmount> costBasisMap = portfolio.getMonies();

        if (costBasisMap.containsKey(TRANSACTION_CODE)) {
            costBasis = costBasisMap.get(TRANSACTION_CODE);
        }

        if (quantityMap.containsKey(TRANSACTION_CODE)) {
            quantity = quantityMap.get(TRANSACTION_CODE);
        }

        switch (transaction.getAction()) {

            // ACB is summed with net amount of purchases
            case Reinvest:
            case Buy:
                quantityMap.put(TRANSACTION_CODE, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                costBasisMap.put(TRANSACTION_CODE, costBasis.add(transaction.getNetAmount()));

                break;

            // ACB per share remains constant during sales.
            case Sell:
                MonetaryAmount acbPerShare = transactionService.getACB(portfolio, TRANSACTION_CODE);
                quantityMap.put(TRANSACTION_CODE, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                costBasisMap.put(TRANSACTION_CODE, acbPerShare.multiply(quantityMap.get(TRANSACTION_CODE).getValue().negate()));

                break;

            // Return of Capital reduces ACB
            case Distribution:
                MonetaryAmount returnOfCapital = transaction.getReturnOfCapital();

                // If a distribution has a RoC component, subtract from ACB
                if (returnOfCapital != null) {
                    costBasisMap.put(TRANSACTION_CODE, costBasis.add(transaction.getReturnOfCapital()));
                }

                break;

            default:
                break;
        }

        if (quantityMap.get(TRANSACTION_CODE).getValue().equals(BigDecimal.ZERO)) {

            costBasisMap.put(TRANSACTION_CODE, Money.of(0, CURRENCY));
        }

        return portfolio
            .withMonies(costBasisMap)
            .withQuantities(quantityMap);
    }
}
