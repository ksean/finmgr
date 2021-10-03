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
package sh.kss.finmgrlib.service;

import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.kss.finmgrlib.data.MarketDataApi;
import sh.kss.finmgrlib.entity.*;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A service to expose useful transaction functions
 *
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private MarketDataApi marketDataApi;

    /**
     * getACB will retrieve the Average Cost Basis from a portfolio given a specific ACB identifier code
     *
     * @param portfolio the portfolio to query
     * @param accountType the account type for the security
     * @param security the security
     * @return the ACB as a MonetaryAmount
     */
    @Override
    public MonetaryAmount getACB(Portfolio portfolio, AccountType accountType, Security security) {

        LOG.debug(String.format("getACB for portfolio=%s, account type=%s and security=%s", portfolio.toString(), accountType, security));

        Holding holding = portfolio.getHoldings().getOrDefault(accountType, Holding.EMPTY);

        // Check if quantity is zero first to avoid zero division
        if (holding.getQuantities().getOrDefault(security, Quantity.ZERO).getValue().equals(BigDecimal.ZERO)) {

            return holding.getCostBasis().getOrDefault(security, Money.of(0, "CAD"));
        }

        return holding.getCostBasis().get(security).divide(holding.getQuantities().get(security).getValue()).negate();
    }

    /**
     * getHistoricalPrices will retrieve the value of all of the positions in a portfolio at a given previous point in time
     *
     * @param portfolio the portfolio to retrieve prices for
     * @param localDate the date to lookup historical prices for
     * @return a map of the securities in a portfolio to their historical EOD market value
     */
    @Override
    public Map<String, MonetaryAmount> getHistoricalPrices(Portfolio portfolio, LocalDate localDate) {

        Map<AccountType, Holding> holdings = portfolio.getHoldings();

        Set<Security> securities = holdings.keySet().stream()
            .map(holdings::get)
            .map(Holding::getSecurities)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        for(Security security : securities) {

            LOG.info(marketDataApi.findClosingPrice(security, localDate).toString());
        }

        return Map.of();
    }
}
