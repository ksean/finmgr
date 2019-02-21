package sh.kss.finmgrlib;

import com.google.common.collect.ImmutableList;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.MonetaryAmount;
import java.util.List;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OperationsTest extends FinmgrTest {

    @Autowired
    Operations operations;

    @Test
    public void zeroQuantityACBTest() {

        // Buy then sell a stock in a single account
        List<InvestmentTransaction> transactions = ImmutableList.of(BUY_VTI, SELL_VTI);

        // Calculate ACB
        MonetaryAmount acb = Operations.currentACB(transactions);

        // Assert $0 ACB
        assertEquals(
            Money.of(0, BASE_CURRENCY),
            acb
        );
    }
}
