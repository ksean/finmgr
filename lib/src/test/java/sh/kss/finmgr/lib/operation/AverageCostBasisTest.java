/*
    finmgr - A financial transaction framework
    Copyright (C) 2024 Kennedy Software Solutions Inc.

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
package sh.kss.finmgr.lib.operation;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sh.kss.finmgr.lib.FinmgrTest;
import sh.kss.finmgr.lib.entity.AccountType;
import sh.kss.finmgr.lib.entity.Portfolio;
import sh.kss.finmgr.lib.entity.Run;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgr.lib.service.TransactionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the average cost basis operation as used in the context of a finmgr Run
 *
 */
@SpringBootTest
public class AverageCostBasisTest extends FinmgrTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AverageCostBasis averageCostBasis;

    private static final Logger LOG = LoggerFactory.getLogger(AverageCostBasisTest.class);


    /**
     * Creates a run that will use the AverageCostBasis operation from a list of transactions
     *
     * @param transactions the list of transactions to calculate ACB for
     * @return the Run object to be used for ACB testing
     */
    private Portfolio operationsTest(List<InvestmentTransaction> transactions) {

        LOG.debug(String.format("Creating a Run from input list of transactions: %s", transactions.toString()));

        return Run.process(
            Portfolio.EMPTY_NON_REGISTERED,
            List.of(averageCostBasis),
            transactions
        );
    }


    /**
     * The resulting ACB from a portfolio with zero net quantity is $0
     *
     */
    @Test
    public void zeroQuantityACBTest() {

        // Buy and sell same quantity of same security
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                SELL_VTI
            )
        );

        // The ACB for the holding should be $0
        assertEquals(
            ZERO_USD,
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * The ACB for multiple different cost purchases should be the gross costs divided by the quantity
     *
     */
    @Test
    public void buyAtDifferentPriceACBTest() {

        // Buy same security twice at different prices
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE
            )
        );

        // The ACB for the holding should be $102.55
        assertEquals(
            Money.of(102.55, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * The act of selling should not change the ACB
     *
     */
    @Test
    public void sellingDoesNotChangeACBTest() {

        // Buy same security twice at different prices then sell some
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                SELL_VTI
            )
        );

        // The ACB for the holding should be $102.55
        assertEquals(
            Money.of(102.55, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * A dividend distribution should not change the ACB
     *
     */
    @Test
    public void dividendDoesNotChangeACBTest() {

        // Buy at two different prices
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                VTI_DIVIDEND
            )
        );

        // The ACB for the holding should be $102.55
        // buy 100 @ $100 / share, + $5 commission
        // buy 100 @ $105 / share, + $5 commission
        // (10_005 + 10_505) / 200 = 102.55
        assertEquals(
            Money.of(102.55, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * A return of capital distribution should reduce the ACB
     *
     */
    @Test
    public void returnOfCapitalReducesACBTest() {

        // Buy at two different prices
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                VTI_RETURN_OF_CAPITAL
            )
        );

        // The ACB for the holding should be $101.55
        // buy 100 @ $100 / share, + $5 commission
        // buy 100 @ $105 / share, + $5 commission
        // roc 200 @ $1 / share
        // (10_005 + 10_505 - 200) / 200 = 101.55
        assertEquals(
            Money.of(101.55, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * Multiple buys then selling all quantity should set ACB to $0
     *
     */
    @Test
    public void sellingAllQuantityResetsACBTest() {

        // Buy at two different prices
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                SELL_VTI,
                SELL_VTI
            )
        );

        // The ACB for the holding should be $0
        assertEquals(
            Money.of(0, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * Selling all quantity then re-buying should reset ACB to the rebuy price
     *
     */
    @Test
    public void sellAllRebuyACBTest() {

        // Buy at two different prices
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                SELL_VTI,
                SELL_VTI,
                BUY_VTI
            )
        );

        // The ACB for the holding should be $100.05
        assertEquals(
            Money.of(100.05, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
    }


    /**
     * Buy the same security in different account types should have isolated ACB
     *
     */
    @Test
    public void buyInMultipleAccountTypes() {

        // Buy at two different prices
        Portfolio portfolio = operationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_TFSA
            )
        );

        // The ACB for the holding should be $100.05
        assertEquals(
            Money.of(100.05, USD),
            transactionService.getACB(portfolio, AccountType.NON_REGISTERED, VTI)
        );
        assertEquals(
            Money.of(100.05, USD),
            transactionService.getACB(portfolio, AccountType.TFSA, VTI)
        );
    }
}
