/*
    finmgr - A financial transaction framework
    Copyright (C) 2020 Kennedy Software Solutions Inc.

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

import java.time.LocalDate;


@Service
public class FinmgrTest {

    protected final Symbol VTI_SYMBOL = new Symbol("VTI");
    protected final Account NON_REG_ACCOUNT = new Account("123-abc", "foo", AccountType.NON_REGISTERED);
    protected final LocalDate BASE_DATE = LocalDate.of(1980, 1, 1);
    protected final Money ZERO_CAD = Money.of(0, Currency.UNIT_CAD);

    // VALID buy and sell transactions
    protected final InvestmentTransaction BUY_VTI = InvestmentTransaction
        .builder()
        .transactionDate(BASE_DATE)
        .settlementDate(BASE_DATE.plusDays(3))
        .action(InvestmentAction.Buy)
        .symbol(VTI_SYMBOL)
        .quantity(Quantity.HUNDRED)
        .description("Buy 100 Shares of VTI at $CAD100")
        .price(Money.of(100, Currency.UNIT_CAD))
        .grossAmount(Money.of(-10_000, Currency.UNIT_CAD))
        .commission(Money.of(-5, Currency.UNIT_CAD))
        .netAmount(Money.of(-10_005, Currency.UNIT_CAD))
        .account(NON_REG_ACCOUNT)
        .returnOfCapital(ZERO_CAD)
        .capitalGain(ZERO_CAD)
        .currency(Currency.CAD)
        .build();


    protected final InvestmentTransaction BUY_VTI_HIGHER_PRICE = BUY_VTI
        .withPrice(Money.of(105, Currency.UNIT_CAD))
        .withGrossAmount(Money.of(-10_500, Currency.UNIT_CAD))
        .withNetAmount(Money.of(-10_505, Currency.UNIT_CAD));

    protected final InvestmentTransaction SELL_VTI = BUY_VTI
        .withAction(InvestmentAction.Sell)
        .withQuantity(new Quantity(BUY_VTI.getQuantity().getValue().negate()))
        .withGrossAmount(BUY_VTI.getGrossAmount().negate())
        .withNetAmount(Money.of(9_995, Currency.UNIT_CAD));

    protected final InvestmentTransaction SELL_VTI_LOWER_PRICE = SELL_VTI
        .withPrice(Money.of(97.50, Currency.UNIT_CAD))
        .withGrossAmount(Money.of(9_750, Currency.UNIT_CAD))
        .withNetAmount(Money.of(9_745, Currency.UNIT_CAD));

    protected final InvestmentTransaction VTI_DIVIDEND = BUY_VTI
        .withAction(InvestmentAction.Distribution)
        .withQuantity(Quantity.ZERO)
        .withPrice(ZERO_CAD)
        .withGrossAmount(ZERO_CAD)
        .withCommission(ZERO_CAD)
        .withNetAmount(Money.of(250, Currency.UNIT_CAD));

    protected final InvestmentTransaction VTI_RETURN_OF_CAPITAL = VTI_DIVIDEND
        .withNetAmount(ZERO_CAD)
        .withReturnOfCapital(Money.of(1, Currency.UNIT_CAD));

    protected final InvestmentTransaction VTI_CAPITAL_GAIN = VTI_RETURN_OF_CAPITAL
        .withReturnOfCapital(ZERO_CAD)
        .withCapitalGain(Money.of(0.75, Currency.UNIT_CAD));

}
