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
    protected final Quantity ZERO_QUANTITY = new Quantity(new BigDecimal(0));
    protected final Account NON_REG_ACCOUNT = new Account("123-abc", "foo", AccountType.NON_REGISTERED);
    protected final Currency CURRENCY = new Currency(BASE_CURRENCY);
    protected final CurrencyUnit BASE_CURRENCY_UNIT = Monetary.getCurrency(BASE_CURRENCY);
    protected final LocalDate BASE_DATE = LocalDate.of(1980, 1, 1);
    protected final Money ZERO_CAD = Money.of(0, BASE_CURRENCY_UNIT);

    // VALID buy and sell transactions
    protected final InvestmentTransaction BUY_VTI = InvestmentTransaction
    .builder()
    .transactionDate(BASE_DATE)
    .settlementDate(BASE_DATE.plusDays(3))
    .action(InvestmentAction.Buy)
    .symbol(VTI_SYMBOL)
    .quantity(HUNDRED_QUANTITY)
    .price(Money.of(100, BASE_CURRENCY_UNIT))
    .grossAmount(Money.of(-10_000, BASE_CURRENCY_UNIT))
    .commission(Money.of(-5, BASE_CURRENCY_UNIT))
    .netAmount(Money.of(-10_005, BASE_CURRENCY_UNIT))
    .account(NON_REG_ACCOUNT)
    .returnOfCapital(ZERO_CAD)
    .capitalGain(ZERO_CAD)
    .currency(CURRENCY)
    .build();


    protected final InvestmentTransaction BUY_VTI_HIGHER_PRICE = BUY_VTI
    .withPrice(Money.of(105, BASE_CURRENCY_UNIT))
    .withGrossAmount(Money.of(-10_500, BASE_CURRENCY_UNIT))
    .withNetAmount(Money.of(-10_505, BASE_CURRENCY_UNIT));

    protected final InvestmentTransaction SELL_VTI = BUY_VTI
    .withAction(InvestmentAction.Sell)
    .withQuantity(new Quantity(BUY_VTI.getQuantity().getValue().negate()))
    .withGrossAmount(BUY_VTI.getGrossAmount().negate())
    .withNetAmount(Money.of(9_995, BASE_CURRENCY_UNIT));

    protected final InvestmentTransaction SELL_VTI_LOWER_PRICE = SELL_VTI
    .withPrice(Money.of(97.50, BASE_CURRENCY_UNIT))
    .withGrossAmount(Money.of(9_750, BASE_CURRENCY_UNIT))
    .withNetAmount(Money.of(9_745, BASE_CURRENCY_UNIT));

    protected final InvestmentTransaction VTI_DIVIDEND = BUY_VTI
    .withAction(InvestmentAction.Distribution)
    .withQuantity(ZERO_QUANTITY)
    .withPrice(ZERO_CAD)
    .withGrossAmount(ZERO_CAD)
    .withCommission(ZERO_CAD)
    .withNetAmount(Money.of(250, BASE_CURRENCY_UNIT));

    protected final InvestmentTransaction VTI_RETURN_OF_CAPITAL = VTI_DIVIDEND
    .withNetAmount(ZERO_CAD)
    .withReturnOfCapital(Money.of(1, BASE_CURRENCY_UNIT));

    protected final InvestmentTransaction VTI_CAPITAL_GAIN = VTI_RETURN_OF_CAPITAL
    .withReturnOfCapital(ZERO_CAD)
    .withCapitalGain(Money.of(0.75, BASE_CURRENCY_UNIT));

}
