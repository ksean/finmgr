/*
    finmgr - A financial transaction framework
    Copyright (C) 2021 Kennedy Software Solutions Inc.

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
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.time.LocalDate;


/**
 * Common finmgr test definitions for use in all tests
 *
 */
public abstract class FinmgrTest {

    protected final Account NON_REG_ACCOUNT = new Account("123-abc", "foo", AccountType.NON_REGISTERED);
    protected final LocalDate BASE_DATE = LocalDate.of(1980, 1, 1);
    protected final CurrencyUnit USD = Monetary.getCurrency("USD");
    protected final CurrencyUnit CAD = Monetary.getCurrency("CAD");
    protected final Security VTI = new Security("VTI", CAD);
    protected final Money ZERO_CAD = Money.of(0, CAD);

    // VALID buy and sell transactions
    // Buying 100 shares of ETF as the root transaction
    protected final InvestmentTransaction BUY_VTI = InvestmentTransaction
        .builder()
        .transactionDate(BASE_DATE)
        .settlementDate(BASE_DATE.plusDays(3))
        .action(InvestmentAction.Buy)
        .security(VTI)
        .quantity(Quantity.HUNDRED)
        .description("Buy 100 Shares of VTI at $CAD100")
        .price(Money.of(100, CAD))
        .grossAmount(Money.of(-10_000, CAD))
        .commission(Money.of(-5, CAD))
        .netAmount(Money.of(-10_005, CAD))
        .account(NON_REG_ACCOUNT)
        .currency(CAD)
        .build();


    protected final InvestmentTransaction BUY_VTI_HIGHER_PRICE = BUY_VTI
        .withPrice(Money.of(105, CAD))
        .withGrossAmount(Money.of(-10_500, CAD))
        .withNetAmount(Money.of(-10_505, CAD));

    protected final InvestmentTransaction SELL_VTI = BUY_VTI
        .withAction(InvestmentAction.Sell)
        .withQuantity(new Quantity(BUY_VTI.getQuantity().getValue().negate()))
        .withGrossAmount(BUY_VTI.getGrossAmount().negate())
        .withNetAmount(Money.of(9_995, CAD));

    protected final InvestmentTransaction SELL_VTI_LOWER_PRICE = SELL_VTI
        .withPrice(Money.of(97.50, CAD))
        .withGrossAmount(Money.of(9_750, CAD))
        .withNetAmount(Money.of(9_745, CAD));

    protected final InvestmentTransaction VTI_DIVIDEND = BUY_VTI
        .withQuantity(null)
        .withDescription("$200 Dividend")
        .withPrice(null)
        .withGrossAmount(null)
        .withCommission(null)
        .withAction(InvestmentAction.Distribution)
        .withNetAmount(Money.of(200, CAD));

    protected final InvestmentTransaction VTI_RETURN_OF_CAPITAL = VTI_DIVIDEND
        .withNetAmount(ZERO_CAD)
        .withReturnOfCapital(Money.of(200, CAD));

    protected final InvestmentTransaction VTI_CAPITAL_GAIN = VTI_RETURN_OF_CAPITAL
        .withReturnOfCapital(ZERO_CAD)
        .withCapitalGain(Money.of(0.75, CAD));

}
