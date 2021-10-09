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
    protected final LocalDate BASE_DATE = LocalDate.of(2010, 1, 1);
    protected final CurrencyUnit USD = Monetary.getCurrency("USD");
    protected final CurrencyUnit CAD = Monetary.getCurrency("CAD");
    protected final Security VTI = new Security("VTI", USD);
    protected final Money ZERO_USD = Money.of(0, USD);

    // VALID buy and sell transactions
    // Buying 100 shares of ETF as the root transaction
    protected final InvestmentTransaction BUY_VTI = InvestmentTransaction
        .builder()
        .transactionDate(BASE_DATE)
        .settlementDate(BASE_DATE.plusDays(3))
        .action(InvestmentAction.Buy)
        .security(VTI)
        .quantity(Quantity.HUNDRED)
        .description("Buy 100 Shares of VTI")
        .price(Money.of(100, USD))
        .grossAmount(Money.of(-10_000, USD))
        .commission(Money.of(-5, USD))
        .netAmount(Money.of(-10_005, USD))
        .account(NON_REG_ACCOUNT)
        .currency(USD)
        .build();

    protected final InvestmentTransaction BUY_VTI_TFSA = BUY_VTI
        .withAccount(new Account("456-def", "foo2", AccountType.TFSA));


    protected final InvestmentTransaction BUY_VTI_HIGHER_PRICE = BUY_VTI
        .withPrice(Money.of(105, USD))
        .withGrossAmount(Money.of(-10_500, USD))
        .withNetAmount(Money.of(-10_505, USD));

    protected final InvestmentTransaction SELL_VTI = BUY_VTI
        .withAction(InvestmentAction.Sell)
        .withDescription("Sell 100 Shares of VTI")
        .withQuantity(new Quantity(BUY_VTI.getQuantity().getValue().negate()))
        .withGrossAmount(BUY_VTI.getGrossAmount().negate())
        .withNetAmount(Money.of(9_995, USD));

    protected final InvestmentTransaction SELL_VTI_LATER = SELL_VTI
        .withTransactionDate(BASE_DATE.plusDays(2))
        .withSettlementDate(BASE_DATE.plusDays(4));

    protected final InvestmentTransaction SELL_VTI_LOWER_PRICE = SELL_VTI
        .withPrice(Money.of(97.50, USD))
        .withGrossAmount(Money.of(9_750, USD))
        .withNetAmount(Money.of(9_745, USD));

    protected final InvestmentTransaction VTI_DIVIDEND = BUY_VTI
        .withQuantity(null)
        .withDescription("$200 Dividend")
        .withPrice(null)
        .withGrossAmount(null)
        .withCommission(null)
        .withAction(InvestmentAction.Distribution)
        .withNetAmount(Money.of(200, USD));

    protected final InvestmentTransaction VTI_RETURN_OF_CAPITAL = VTI_DIVIDEND
        .withNetAmount(ZERO_USD)
        .withReturnOfCapital(Money.of(200, USD));

    protected final InvestmentTransaction VTI_CAPITAL_GAIN = VTI_RETURN_OF_CAPITAL
        .withReturnOfCapital(ZERO_USD)
        .withCapitalGain(Money.of(0.75, USD));

}
