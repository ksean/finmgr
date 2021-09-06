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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.service.TransactionServiceImpl;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static sh.kss.finmgrlib.entity.Quantity.ZERO;

/**
 * Average cost basis will enhance a portfolio with data necessary to calculate each holdings ACB, given an input
 * investment transaction
 *
 */
@Component
@AllArgsConstructor
public class AverageCostBasis implements Operation {

    private final TransactionServiceImpl transactionService;
    private static final String COST_BASIS = "ACB_CB";
    private static final String QUANTITY = "ACB_Q";

    @Override
    public Portfolio process(Portfolio portfolio, InvestmentTransaction transaction) {

        // ACB changes are limited to specific symbols so short circuit if not present
        Security security = transaction.getSecurity();
        if (security == null) {
            return portfolio;
        }

        final AccountType ACCOUNT_TYPE = transaction.getAccount().getAccountType();
        final CurrencyUnit CURRENCY = transaction.getCurrency();
        Map<AccountType, Holding> holdings = Maps.newHashMap(portfolio.getHoldings());
        Holding holding = holdings.getOrDefault(ACCOUNT_TYPE, Holding.EMPTY);

        // Get ACB Map for the cursor cost basis
        Map<Security, MonetaryAmount> costBases = Maps.newHashMap(holding.getCostBasis());
        MonetaryAmount costBasis = costBases.getOrDefault(security, Money.of(0, CURRENCY));

        Map<Security, Quantity> quantities = Maps.newHashMap(holding.getQuantities());
        Quantity quantity = quantities.getOrDefault(security, ZERO);

        Set<Security> securities = Sets.newHashSet(holding.getSecurities());

        switch (transaction.getAction()) {

            // ACB is summed with net amount of purchases
            case Reinvest:
            case Buy:
                quantities.put(security, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                costBases.put(security, costBasis.add(transaction.getNetAmount()));
                securities.add(security);
                break;

            // ACB per share remains constant during sales.
            case Sell:
                MonetaryAmount acbPerShare = transactionService.getACB(portfolio, ACCOUNT_TYPE, security);
                quantities.put(security, quantity.withValue(quantity.getValue().add(transaction.getQuantity().getValue())));
                costBases.put(security, acbPerShare.multiply(quantities.get(security).getValue().negate()));
                break;

            // Return of Capital reduces ACB
            case Distribution:
                MonetaryAmount returnOfCapital = transaction.getReturnOfCapital();

                // If a distribution has a RoC component, subtract from ACB
                if (returnOfCapital != null) {
                    costBases.put(security, costBasis.add(transaction.getReturnOfCapital()));
                }
                break;

            default:
                break;
        }

        // If the quantity for a security is reduced to zero (sold all units), reset ACB
        // TODO: Superficial loss rule?
        if (quantities.get(security).getValue().equals(BigDecimal.ZERO)) {

            costBases.put(security, Money.of(0, CURRENCY));
            securities.remove(security);
        }

        return getNewPortfolio(portfolio,  ACCOUNT_TYPE, costBases, quantities, securities);
    }

    private Portfolio getNewPortfolio(Portfolio oldPortfolio, AccountType accountType, Map<Security, MonetaryAmount> costBases, Map<Security, Quantity> quantities, Set<Security> securities) {
        return oldPortfolio
            .withHoldings(
                Map.of(accountType,
                    new Holding(Set.copyOf(securities), Map.copyOf(quantities), Map.copyOf(costBases))
                )
            );
    }
}
