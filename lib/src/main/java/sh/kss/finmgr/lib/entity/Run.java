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
package sh.kss.finmgr.lib.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgr.lib.operation.DailyOperation;
import sh.kss.finmgr.lib.operation.TransactionOperation;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A run is a list of transactions and some set of operations to run with each iteration
 *
 */
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
        for (InvestmentTransaction transaction : transactions) {

            LOG.debug("Transaction: " + transaction.getDescription());

            // Iterate through all operations
            for (TransactionOperation transactionOperation : transactionOperations) {

                LOG.debug("Operation: " + transactionOperation.toString());

                portfolio = transactionOperation.process(portfolio, transaction);

                LOG.debug("Portfolio: " + portfolio.toString());
            }
        }

        return portfolio;
    }

    public static Map<LocalDate, Map<AccountType, Map<String, Map<Security, MonetaryAmount>>>> process(Portfolio portfolio, List<TransactionOperation> transactionOperations, List<InvestmentTransaction> transactions, List<DailyOperation> dailyOperations, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<InvestmentTransaction>> dailyTransactions = transactions.stream().collect(Collectors.groupingBy(InvestmentTransaction::getTransactionDate));
        List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());

        Map<LocalDate, Map<AccountType, Map<String, Map<Security, MonetaryAmount>>>> resultSet = new HashMap<>();
        for (LocalDate date : dates) {
            if (dailyTransactions.containsKey(date)) {
                portfolio = process(portfolio, transactionOperations, dailyTransactions.get(date));
            }

            Map<AccountType, Map<String, Map<Security, MonetaryAmount>>> accountResults = new HashMap<>();
            for (AccountType accountType : portfolio.getHoldings().keySet()) {
                Map<String, Map<Security, MonetaryAmount>> securityResults = new HashMap<>();
                for (DailyOperation dailyOperation : dailyOperations) {
                    securityResults.put(dailyOperation.getName(), dailyOperation.process(portfolio.getHoldings().get(accountType), date));
                }
                accountResults.put(accountType, securityResults);
            }
            resultSet.put(date, accountResults);
        }

        return resultSet;
    }
}
