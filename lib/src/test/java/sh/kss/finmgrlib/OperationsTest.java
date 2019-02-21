package sh.kss.finmgrlib;

import com.google.common.collect.ImmutableList;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.MonetaryAmount;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OperationsTest extends FinmgrTest {

    @Test
    public void zeroQuantityACBTest() {

        // Buy then sell a stock in a single account
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            SELL_VTI
        );

        // Calculate ACB
        MonetaryAmount acb = Operations.currentACB(transactions);

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
        MonetaryAmount acb = Operations.currentACB(transactions);

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
        MonetaryAmount acb = Operations.currentACB(transactions);

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
        MonetaryAmount acb = Operations.currentACB(transactions);

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
        MonetaryAmount acb = Operations.currentACB(transactions);

        // Assert $CAD101.55 ACB -> RoC $1 per share
        assertEquals(
            Money.of(101.55, BASE_CURRENCY),
            acb
        );
    }

    @Test
    public void capitalGainIncreasesACBTest() {

        // Buy at two different prices
        List<InvestmentTransaction> transactions = ImmutableList.of(
            BUY_VTI,
            BUY_VTI_HIGHER_PRICE,
            VTI_CAPITAL_GAIN
        );

        // Calculate ACB
        MonetaryAmount acb = Operations.currentACB(transactions);

        // Assert $CAD103.30 ACB -> Capital gain $0.75 per share
        assertEquals(
            Money.of(103.30, BASE_CURRENCY),
            acb
        );
    }
}
