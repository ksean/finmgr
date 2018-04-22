package sh.kss.finmgrlib;


import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OperationsTest {

    @Autowired
    Operations operations;

    MonetaryAmount price;
    MonetaryAmount grossAmount;
    MonetaryAmount commission;
    MonetaryAmount netAmount;
    Account account;
    MonetaryAmount returnOnCapital;
    MonetaryAmount capitalGain;
    Currency currency;

    private final String BASE_CURRENCY = "CAD";


    InvestmentTransaction buyVTI = new InvestmentTransaction(
            LocalDate.now(),
            LocalDate.now().plusDays(3),
            InvestmentAction.Buy,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(100)),
            Money.of(100, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(10_000, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-5, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(9_995, Monetary.getCurrency(BASE_CURRENCY)),
            new Account("123-abc", "foo"),
            Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
            new Currency(BASE_CURRENCY)
    );

    InvestmentTransaction sellVTI = new InvestmentTransaction(
            LocalDate.now(),
            LocalDate.now().plusDays(3),
            InvestmentAction.Sell,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(-100)),
            Money.of(100, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-10_000, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-5, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(9_995, Monetary.getCurrency(BASE_CURRENCY)),
            new Account("123-abc", "foo"),
            Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
            new Currency(BASE_CURRENCY)
    );

    @Test
    public void inconsistentCurrencyTest() {
        CurrencyUnit cad = Monetary.getCurrency("CAD");
        CurrencyUnit usd = Monetary.getCurrency("USD");

        InvestmentTransaction inconsistentTransaction = new InvestmentTransaction(
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                InvestmentAction.Buy,
                new Symbol("VTI"),
                new Quantity(new BigDecimal(100)),
                Money.of(100, cad),
                Money.of(10_000, cad),
                Money.of(-5, cad),
                Money.of(9_995, usd), // Inconsistent
                new Account("123-abc", "foo"),
                Money.of(0, cad),
                Money.of(0, cad),
                new Currency(BASE_CURRENCY)
        );

        assertFalse(inconsistentTransaction.currencyIsConsistent());
    }

    @Test
    public void consistentCurrencyTest() {

        assertTrue(buyVTI.currencyIsConsistent());
    }
}
