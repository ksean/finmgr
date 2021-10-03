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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import sh.kss.finmgrlib.data.MarketDataApi;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.inject.Singleton;
import javax.money.CurrencyUnit;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static sh.kss.finmgrlib.entity.Quantity.ZERO;

@Component
@Singleton
@AllArgsConstructor
public class NetPresentValue implements TransactionOperation {

    private final MarketDataApi dataApi;

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

        Map<Security, Quantity> quantities = Maps.newHashMap(holding.getQuantities());
        Quantity quantity = quantities.getOrDefault(security, ZERO);

        //TODO: Implement
        switch (transaction.getAction()) {

            case Reinvest:
            case Buy:

                break;

            case Sell:

                break;

            case Distribution:
                break;

            default:
                break;
        }

        if (quantities.get(security).getValue().equals(BigDecimal.ZERO)) {


        }

        return getNewPortfolio(portfolio, ACCOUNT_TYPE, new Holding(holding.getSecurities(), quantities, holding.getCostBasis()));
    }

    private Portfolio getNewPortfolio(Portfolio oldPortfolio, AccountType accountType, Holding holding) {
        // Build new Holdings map
        HashMap<AccountType, Holding> newHoldings = new HashMap<>();
        newHoldings.put(accountType, holding);
        for (AccountType oldType : oldPortfolio.getHoldings().keySet()) {
            if (!oldType.equals(accountType)) {
                newHoldings.put(oldType, oldPortfolio.getHoldings().get(oldType));
            }
        }

        return oldPortfolio
            .withHoldings(ImmutableMap.copyOf(newHoldings));
    }
}
