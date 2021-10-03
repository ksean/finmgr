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
package sh.kss.finmgrlib.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.operation.TransactionOperation;

import javax.inject.Singleton;
import java.util.List;


/**
 * A run is a list of transactions and some set of operations to run with each iteration
 *
 */
@Singleton
public class Run {

    // Log manager
    private static final Logger LOG = LoggerFactory.getLogger(Run.class);

    /**
     * Perform the operations against a list of transactions
     *
     * @return the final state of the portfolio after performing all operations
     */
    public static Portfolio process(Portfolio portfolio, List<TransactionOperation> transactionOperations, List<InvestmentTransaction> transactions) {

        // Iterate through all transactions
        for(InvestmentTransaction transaction : transactions) {

            LOG.debug("Transaction: " + transaction.getDescription());

            // Iterate through all operations
            for(TransactionOperation transactionOperation : transactionOperations) {

                LOG.debug("Operation: " + transactionOperation.toString());

                portfolio = transactionOperation.process(portfolio, transaction);

                LOG.debug("Portfolio: " + portfolio.toString());
            }
        }

        return portfolio;
    }
}
