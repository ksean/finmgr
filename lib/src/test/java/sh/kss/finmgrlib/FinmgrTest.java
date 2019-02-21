package sh.kss.finmgrlib;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class FinmgrTest {

    protected final String BASE_CURRENCY = "CAD";
    protected final Symbol VTI_SYMBOL = new Symbol("VTI");
    protected final Quantity HUNDRED_QUANTITY = new Quantity(new BigDecimal(100));
    protected final Account NON_REG_ACCOUNT = new Account("123-abc", "foo", AccountType.NON_REGISTERED);
    protected final Currency CURRENCY = new Currency(BASE_CURRENCY);
    protected final CurrencyUnit BASE_CURRENCY_UNIT = Monetary.getCurrency(BASE_CURRENCY);
    protected final LocalDate BASE_DATE = LocalDate.of(1980, 1, 1);

    // VALID buy and sell transactions
    protected final InvestmentTransaction BUY_VTI = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Buy,
        VTI_SYMBOL,
        HUNDRED_QUANTITY,
        Money.of(100, BASE_CURRENCY_UNIT),
        Money.of(-10_000, BASE_CURRENCY_UNIT),
        Money.of(-5, BASE_CURRENCY_UNIT),
        Money.of(-10_005, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );

    protected final InvestmentTransaction BUY_VTI_HIGHER_PRICE = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Buy,
        VTI_SYMBOL,
        HUNDRED_QUANTITY,
        Money.of(105, BASE_CURRENCY_UNIT),
        Money.of(-10_500, BASE_CURRENCY_UNIT),
        Money.of(-5, BASE_CURRENCY_UNIT),
        Money.of(-10_505, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );

    protected final InvestmentTransaction SELL_VTI = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Sell,
        VTI_SYMBOL,
        new Quantity(new BigDecimal(-100)),
        Money.of(100, BASE_CURRENCY_UNIT),
        Money.of(10_000, BASE_CURRENCY_UNIT),
        Money.of(-5, BASE_CURRENCY_UNIT),
        Money.of(9_995, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );

    protected final InvestmentTransaction SELL_VTI_LOWER_PRICE = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Sell,
        VTI_SYMBOL,
        new Quantity(new BigDecimal(-10)),
        Money.of(97.50, BASE_CURRENCY_UNIT),
        Money.of(975, BASE_CURRENCY_UNIT),
        Money.of(-5, BASE_CURRENCY_UNIT),
        Money.of(970, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );

    protected final InvestmentTransaction VTI_DIVIDEND = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Distribution,
        VTI_SYMBOL,
        new Quantity(new BigDecimal(0)),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(250, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );

    protected final InvestmentTransaction VTI_ROC = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Distribution,
        VTI_SYMBOL,
        new Quantity(new BigDecimal(0)),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(250, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );

    protected final InvestmentTransaction VTI_CAPITALGAIN = new InvestmentTransaction(
        BASE_DATE,
        BASE_DATE.plusDays(3),
        InvestmentAction.Distribution,
        VTI_SYMBOL,
        new Quantity(new BigDecimal(0)),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(250, BASE_CURRENCY_UNIT),
        NON_REG_ACCOUNT,
        Money.of(0, BASE_CURRENCY_UNIT),
        Money.of(0, BASE_CURRENCY_UNIT),
        CURRENCY
    );
}
