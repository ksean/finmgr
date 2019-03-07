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
package sh.kss.finmgrlib.operation;

import com.google.common.collect.ImmutableList;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.FinmgrTest;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.MonetaryAmount;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OperationTest extends FinmgrTest {

    @Test
    public void zeroQuantityACBTest() {

        // Buy then sell a stock in a single account
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            SELL_VTI
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $0 ACB
        assertEquals(
            Money.of(0, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void buyAtDifferentPriceACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $CAD102.55 ACB
        assertEquals(
            Money.of(102.55, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void sellingDoesNotChangeACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE,
            SELL_VTI
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $CAD102.55 ACB
        assertEquals(
            Money.of(102.55, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void dividendDoesNotChangeACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE,
            VTI_DIVIDEND
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $CAD102.55 ACB
        assertEquals(
            Money.of(102.55, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void returnOfCapitalReducesACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE,
            VTI_RETURN_OF_CAPITAL
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $CAD101.55 ACB -> RoC $1 per share
        assertEquals(
            Money.of(101.55, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void sellingAllQuantityResetsACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE,
            SELL_VTI,
            SELL_VTI
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $CAD0 ACB -> sold all units
        assertEquals(
            Money.of(0, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void sellAllRebuyACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE,
            SELL_VTI,
            SELL_VTI,
            BUY_VTI
        );

        // Calculate ACB
        MonetaryAmount acb = Operation.currentACB(transactions);

        // Assert $CAD100.05 ACB -> sold all units, then rebuy @ $100
        assertEquals(
            Money.of(100.05, BASE_CURRENCY),
            acb
        );
    }
}
