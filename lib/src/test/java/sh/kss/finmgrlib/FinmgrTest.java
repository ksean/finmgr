package sh.kss.finmgrlib;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class FinmgrTest {

    protected final String BASE_CURRENCY = "CAD";

    // VALID buy and sell transactions
    protected final InvestmentTransaction BUY_VTI = new InvestmentTransaction(
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

    protected final InvestmentTransaction BUY_VTI_HIGHER_PRICE = new InvestmentTransaction(
        LocalDate.now(),
        LocalDate.now().plusDays(3),
        InvestmentAction.Buy,
        new Symbol("VTI"),
        new Quantity(new BigDecimal(100)),
        Money.of(105, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(-10_500, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(-5, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(-10_505, Monetary.getCurrency(BASE_CURRENCY)),
        new Account("123-abc", "foo", AccountType.NON_REGISTERED),
        Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
        new Currency(BASE_CURRENCY)
    );

    protected final InvestmentTransaction SELL_VTI = new InvestmentTransaction(
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

    protected final InvestmentTransaction SELL_VTI_LOWER_PRICE = new InvestmentTransaction(
        LocalDate.now(),
        LocalDate.now().plusDays(3),
        InvestmentAction.Sell,
        new Symbol("VTI"),
        new Quantity(new BigDecimal(-10)),
        Money.of(97.50, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(975, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(-5, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(970, Monetary.getCurrency(BASE_CURRENCY)),
        new Account("123-abc", "foo", AccountType.NON_REGISTERED),
        Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
        Money.of(0, Monetary.getCurrency(BASE_CURRENCY)),
        new Currency(BASE_CURRENCY)
    );
}
