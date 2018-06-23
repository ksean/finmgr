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
public class InvestmentTransactionTest {

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

    // VALID buy and sell transactions
    InvestmentTransaction buyVTI = new InvestmentTransaction(
            LocalDate.now(),
            LocalDate.now().plusDays(3),
            InvestmentAction.Buy,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(100)),
            Money.of(100, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-10_000, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-5, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-10_005, Monetary.getCurrency(BASE_CURRENCY)),
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
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
            Money.of(10_000, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(-5, Monetary.getCurrency(BASE_CURRENCY)),
            Money.of(9_995, Monetary.getCurrency(BASE_CURRENCY)),
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
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
                new Account("123-abc", "foo", AccountType.NON_REGISTERED),
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

    @Test
    public void settledOnOrAfterTransactionDateTest() {

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction settledSameDay = new InvestmentTransaction(
                now, // Same transaction and settlement dates
                now,
                InvestmentAction.Buy,
                new Symbol("VTI"),
                new Quantity(new BigDecimal(100)),
                Money.of(100, cad),
                Money.of(10_000, cad),
                Money.of(-5, cad),
                Money.of(9_995, cad),
                new Account("123-abc", "foo", AccountType.NON_REGISTERED),
                Money.of(0, cad),
                Money.of(0, cad),
                new Currency(BASE_CURRENCY)
        );

        assertTrue(buyVTI.settledAfterTransaction());
        assertTrue(settledSameDay.settledAfterTransaction());
    }

    @Test
    public void settledBeforeTransactionDateTest() {

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction badSettlementDateTransaction = new InvestmentTransaction(
                now,
                now.minusDays(3), // Settled before transaction
                InvestmentAction.Buy,
                new Symbol("VTI"),
                new Quantity(new BigDecimal(100)),
                Money.of(100, cad),
                Money.of(10_000, cad),
                Money.of(-5, cad),
                Money.of(9_995, cad),
                new Account("123-abc", "foo", AccountType.NON_REGISTERED),
                Money.of(0, cad),
                Money.of(0, cad),
                new Currency(BASE_CURRENCY)
        );

        assertFalse(badSettlementDateTransaction.settledAfterTransaction());
    }

    @Test
    public void validGrossAmountTest() {

        assertTrue(buyVTI.grossAmountEqualsProductOfQuantityPrice());
        assertTrue(sellVTI.grossAmountEqualsProductOfQuantityPrice());
    }

    @Test
    public void invalidGrossAmountTest() {

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction invalidGrossAmountTransaction = new InvestmentTransaction(
                now,
                now.minusDays(3), // Settled before transaction
                InvestmentAction.Buy,
                new Symbol("VTI"),
                new Quantity(new BigDecimal(100)),
                Money.of(100, cad),
                Money.of(-10_001, cad),
                Money.of(-5, cad),
                Money.of(-10_006, cad),
                new Account("123-abc", "foo", AccountType.NON_REGISTERED),
                Money.of(0, cad),
                Money.of(0, cad),
                new Currency(BASE_CURRENCY)
        );

        assertFalse(invalidGrossAmountTransaction.grossAmountEqualsProductOfQuantityPrice());
    }

    @Test
    public void validNetAmountTest() {

        assertTrue(buyVTI.netAmountEqualsGrossMinusCommission());
        assertTrue(sellVTI.netAmountEqualsGrossMinusCommission());
    }

    @Test
    public void invalidNetAmountTest() {

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction invalidNetAmountTransaction = new InvestmentTransaction(
                now,
                now.minusDays(3), // Settled before transaction
                InvestmentAction.Buy,
                new Symbol("VTI"),
                new Quantity(new BigDecimal(100)),
                Money.of(100, cad),
                Money.of(-10_000, cad),
                Money.of(-5, cad),
                Money.of(-10_004, cad),
                new Account("123-abc", "foo", AccountType.NON_REGISTERED),
                Money.of(0, cad),
                Money.of(0, cad),
                new Currency(BASE_CURRENCY)
        );

        assertFalse(invalidNetAmountTransaction.netAmountEqualsGrossMinusCommission());
    }
}
