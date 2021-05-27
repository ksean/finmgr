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
import sh.kss.finmgrlib.entity.*;
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
    private static final String COST_BASIS = "ACB_CB";
    private static final String QUANTITY = "ACB_Q";

    @Autowired
    public AverageCostBasis(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Portfolio process(Portfolio portfolio, InvestmentTransaction transaction) {

        // ACB changes are limited to specific symbols so short circuit if not present
        Security security = transaction.getSecurity();
        if (security == null) {
            return portfolio;
        }

        final AccountType ACCOUNT_TYPE = transaction.getAccount().getAccountType();
        final CurrencyUnit CURRENCY = transaction.getCurrency();
        Map<AccountType, Holding> holdings = portfolio.getHoldings();
        Holding holding = holdings.getOrDefault(ACCOUNT_TYPE, Holding.EMPTY);

        if (holding.equals(Holding.EMPTY)) {

            return portfolio;
        }

        // Get ACB Map for the cursor cost basis
        Map<Security, MonetaryAmount> costBasisMap = holding.getCostBasis();
        MonetaryAmount costBasis = costBasisMap.getOrDefault(security, Money.of(0, CURRENCY));

        Map<Security, Quantity> quantityMap = holding.getQuantities();
        Quantity quantity = quantityMap.getOrDefault(security, ZERO);

        switch (transaction.getAction()) {

            // ACB is summed with net amount of purchases
            case Reinvest:
            case Buy:
                quantityMap.put(security, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                costBasisMap.put(security, costBasis.add(transaction.getNetAmount()));

                break;

            // ACB per share remains constant during sales.
            case Sell:
                MonetaryAmount acbPerShare = transactionService.getACB(portfolio, ACCOUNT_TYPE, security);
                quantityMap.put(security, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                costBasisMap.put(security, acbPerShare.multiply(quantityMap.get(security).getValue().negate()));

                break;

            // Return of Capital reduces ACB
            case Distribution:
                MonetaryAmount returnOfCapital = transaction.getReturnOfCapital();

                // If a distribution has a RoC component, subtract from ACB
                if (returnOfCapital != null) {
                    costBasisMap.put(security, costBasis.add(transaction.getReturnOfCapital()));
                }

                break;

            default:
                break;
        }

        // If the quantity for a security is reduced to zero (sold all units), reset ACB
        // TODO: Superficial loss rule?
        if (quantityMap.get(security).getValue().equals(BigDecimal.ZERO)) {

            costBasisMap.put(security, Money.of(0, CURRENCY));
        }



        return portfolio
            .withHoldings(holdings);
    }
}
