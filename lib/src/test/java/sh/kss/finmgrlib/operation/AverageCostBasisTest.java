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
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.FinmgrTest;
import sh.kss.finmgrlib.entity.Portfolio;
import sh.kss.finmgrlib.entity.Run;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@SpringBootTest
@RunWith(SpringRunner.class)
public class AverageCostBasisTest extends FinmgrTest {

    private final List<Operation> OPERATIONS = Lists.newArrayList(new AverageCostBasis());
    private final String TXCODE = "NON_REGISTERED-VTI-ACB";

    private Run operationsTest(List<InvestmentTransaction> transactions) {

        return Run.builder()
            .portfolio(
                Portfolio.builder()
                    .monies(Maps.newHashMap())
                    .quantities(Maps.newHashMap())
                    .build()
            )
            .operations(OPERATIONS)
            .transactions(transactions)
            .build();
    }


    @Test
    public void zeroQuantityACBTest() {

        // Buy and sell same quantity of same security
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                SELL_VTI
            )
        );

        assertEquals(
            ZERO_CAD,
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }


    @Test
    public void buyAtDifferentPriceACBTest() {

        // Buy same security twice at different prices
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE
            )
        );

        assertEquals(
            Money.of(102.55, BASE_CURRENCY),
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }


    @Test
    public void sellingDoesNotChangeACBTest() {

        // Buy same security twice at different prices then sell some
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                SELL_VTI
            )
        );

        assertEquals(
            Money.of(102.55, BASE_CURRENCY),
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }


    @Test
    public void dividendDoesNotChangeACBTest() {

        // Buy at two different prices
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                VTI_DIVIDEND
            )
        );

        assertEquals(
            Money.of(102.55, BASE_CURRENCY),
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }


    @Test
    public void returnOfCapitalReducesACBTest() {

        // Buy at two different prices
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                VTI_RETURN_OF_CAPITAL
            )
        );

        assertEquals(
            Money.of(101.55, BASE_CURRENCY),
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }


    @Test
    public void sellingAllQuantityResetsACBTest() {

        // Buy at two different prices
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                SELL_VTI,
                SELL_VTI
            )
        );

        assertEquals(
            Money.of(0, BASE_CURRENCY),
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }


    @Test
    public void sellAllRebuyACBTest() {

        // Buy at two different prices
        Run test = operationsTest(
            ImmutableList.of(
                BUY_VTI,
                BUY_VTI_HIGHER_PRICE,
                SELL_VTI,
                SELL_VTI,
                BUY_VTI
            )
        );

        assertEquals(
            Money.of(100.05, BASE_CURRENCY),
            AverageCostBasis.getACB(Run.process(test), TXCODE)
        );
    }
}
