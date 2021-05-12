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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.kss.finmgrlib.entity.Portfolio;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

/**
 * A service to expose useful transaction functions
 *
 */
@Service
public class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    /**
     * getACB will retrieve the Average Cost Basis from a portfolio given a specific ACB identifier code
     *
     * @param portfolio the portfolio to query
     * @param txcode the ACB identifier code
     * @return the ACB as a MonetaryAmount
     */
    public MonetaryAmount getACB(Portfolio portfolio, String txcode) {

        LOG.debug(String.format("getACB for portfolio %s and txcode %s", portfolio.toString(), txcode));

        // Check if quantity is zero first to avoid zero division
        if (portfolio.getQuantities().get(txcode).getValue().equals(BigDecimal.ZERO)) {


            return portfolio.getMonies().get(txcode);
        }

        return portfolio.getMonies().get(txcode).divide(portfolio.getQuantities().get(txcode).getValue()).negate();
    }
}
