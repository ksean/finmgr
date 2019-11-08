/*
    finmgr - A financial transaction framework
    Copyright (C) 2019 Kennedy Software Solutions Inc.

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

import lombok.Builder;
import lombok.Value;
import lombok.With;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.operation.Operation;

import java.util.List;


/**
 * A run is a list of transactions and some set of operations to run with each iteration
 */
@Value
@With
@Builder(toBuilder = true)
public class Run {

    // Given a starting portfolio
    Portfolio portfolio;

    // Perform all of these operations
    List<Operation> operations;

    // When iterating through this list of transactions
    List<InvestmentTransaction> transactions;

    /**
     * Perform the operations against a list of transactions
     *
     * @return the final state of the portfolio after performing all operations
     */
    public static Portfolio process(Run run) {

        // Iterate through all transactions
        for(InvestmentTransaction transaction : run.transactions) {

            // Iterate through all operations
            for(Operation operation : run.operations) {

                // Update the portfolio with the results of performing the operation
                run = run.withPortfolio(operation.process(run.portfolio, transaction));
            }
        }

        return run.getPortfolio();
    }
}
