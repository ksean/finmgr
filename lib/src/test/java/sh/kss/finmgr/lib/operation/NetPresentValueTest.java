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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sh.kss.finmgr.lib.FinmgrTest;
import sh.kss.finmgr.lib.data.MarketDataApi;
import sh.kss.finmgr.lib.entity.AccountType;
import sh.kss.finmgr.lib.entity.Portfolio;
import sh.kss.finmgr.lib.entity.Run;
import sh.kss.finmgr.lib.entity.Security;
import sh.kss.finmgr.lib.entity.transaction.InvestmentTransaction;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the net present value daily operation reporting
 *
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class NetPresentValueTest extends FinmgrTest {

    @Mock
    private MarketDataApi marketDataApiMock;

    @InjectMocks
    private NetPresentValue netPresentValue;

    @Autowired
    private AverageCostBasis averageCostBasis;

    private static final Logger LOG = LoggerFactory.getLogger(NetPresentValueTest.class);


    /**
     * Creates a run that will use the NetPresentValue operation from a list of transactions
     *
     * @param transactions the list of transactions to calculate NPV for
     * @param startDate the start date for the daily operations test
     * @param endDate the end date for the daily operations test (inclusive)
     * @return the Report result object to be used for NPV testing
     */
    private Map<LocalDate, Map<AccountType, Map<String, Map<Security, MonetaryAmount>>>> dailyOperationsTest(List<InvestmentTransaction> transactions, LocalDate startDate, LocalDate endDate) {

        LOG.debug(String.format("Creating a Run from input list of transactions: %s", transactions.toString()));

        return Run.process(
            Portfolio.EMPTY_NON_REGISTERED,
            List.of(averageCostBasis),
            transactions,
            List.of(netPresentValue),
            startDate,
            endDate
        );
    }


    /**
     * The resulting NPV from a portfolio with zero net quantity is $0
     *
     */
    @Test
    public void zeroQuantityNpvTest() {
        LocalDate startDate = BASE_DATE;
        LocalDate midDate = BASE_DATE.plusDays(1);
        LocalDate endDate = BASE_DATE.plusDays(2);

        // Mock internet requests
        Mockito.when(marketDataApiMock.findClosingPrice(VTI, startDate)).thenReturn(Optional.of(Money.of(50, USD)));
        Mockito.when(marketDataApiMock.findClosingPrice(VTI, midDate)).thenReturn(Optional.of(Money.of(51, USD)));
        Mockito.when(marketDataApiMock.findClosingPrice(VTI, endDate)).thenReturn(Optional.of(Money.of(52, USD)));

        // Buy and sell same quantity of same security
        Map<LocalDate, Map<AccountType, Map<String, Map<Security, MonetaryAmount>>>> result = dailyOperationsTest(
            List.of(
                BUY_VTI,
                SELL_VTI_LATER
            ),
            startDate,
            endDate
        );

        // The NPV for the holding should be non-zero after buying and $0 after sold
        // Expecting 3 report dates
        assertEquals(3, result.size());
        // Each day should have just 1 account type of non-registered
        assertEquals(1, result.get(startDate).size());
        assertTrue(result.get(startDate).containsKey(AccountType.NON_REGISTERED));
        // Each account type report should have just 1 daily operation being reported of NPV
        assertEquals(1, result.get(startDate).get(AccountType.NON_REGISTERED).size());
        assertTrue(result.get(startDate).get(AccountType.NON_REGISTERED).containsKey(netPresentValue.getName()));
        // NPV should be reporting on just 1 security VTI
        assertEquals(1, result.get(startDate).get(AccountType.NON_REGISTERED).get(netPresentValue.getName()).size());
        assertTrue(result.get(startDate).get(AccountType.NON_REGISTERED).get(netPresentValue.getName()).containsKey(VTI));
        // The NPV after buying should be 5_000
        assertEquals(Money.of(5_000, USD), result.get(startDate).get(AccountType.NON_REGISTERED).get(netPresentValue.getName()).get(VTI));
        // The NPV on the second day should be 5_100
        assertEquals(Money.of(5_100, USD), result.get(midDate).get(AccountType.NON_REGISTERED).get(netPresentValue.getName()).get(VTI));
        // The NPV after selling should be zero
        assertTrue(result.get(endDate).get(AccountType.NON_REGISTERED).get(netPresentValue.getName()).get(VTI).isZero());
    }

    /**
     * The resulting NPV from a portfolio with holdings in multiple account types is segregated
     *
     */
    @Test
    public void segregatedAccountTypeNpvTest() {
        LocalDate startDate = BASE_DATE;
        LocalDate midDate = BASE_DATE.plusDays(1);
        LocalDate endDate = BASE_DATE.plusDays(2);

        // Mock internet requests
        Mockito.when(marketDataApiMock.findClosingPrice(VTI, startDate)).thenReturn(Optional.of(Money.of(50, USD)));
        Mockito.when(marketDataApiMock.findClosingPrice(VTI, midDate)).thenReturn(Optional.of(Money.of(51, USD)));
        Mockito.when(marketDataApiMock.findClosingPrice(VTI, endDate)).thenReturn(Optional.of(Money.of(52, USD)));

        // Buy and sell same quantity of same security
        Map<LocalDate, Map<AccountType, Map<String, Map<Security, MonetaryAmount>>>> result = dailyOperationsTest(
            List.of(
                BUY_VTI,
                BUY_VTI_TFSA
            ),
            startDate,
            endDate
        );

        // The NPV for the holding should be non-zero after buying and $0 after sold
        // Expecting 3 report dates
        assertEquals(3, result.size());
        // Each day should have just 2 account types
        assertEquals(2, result.get(startDate).size());
        assertEquals(2, result.get(midDate).size());
        assertEquals(2, result.get(endDate).size());
        assertTrue(result.get(startDate).containsKey(AccountType.NON_REGISTERED));
        assertTrue(result.get(startDate).containsKey(AccountType.TFSA));
        // Each account type report should have just 1 daily operation being reported of NPV
        Map<String, Map<Security, MonetaryAmount>> accountResult = result.get(startDate).get(AccountType.TFSA);
        assertEquals(1, result.get(startDate).get(AccountType.TFSA).size());
        assertTrue(accountResult.containsKey(netPresentValue.getName()));
        // NPV should be reporting on just 1 security VTI
        Map<Security, MonetaryAmount> npvResult = accountResult.get(netPresentValue.getName());
        assertEquals(1, npvResult.size());
        assertTrue(npvResult.containsKey(VTI));
        // The NPV after buying should be 5_000
        assertEquals(Money.of(5_000, USD), npvResult.get(VTI));
        // The NPV on the second day should be 5_100
        assertEquals(Money.of(5_100, USD), result.get(midDate).get(AccountType.TFSA).get(netPresentValue.getName()).get(VTI));
        // The NPV on the last day should be 5_200
        assertEquals(Money.of(5_200, USD), result.get(endDate).get(AccountType.TFSA).get(netPresentValue.getName()).get(VTI));
    }
}
