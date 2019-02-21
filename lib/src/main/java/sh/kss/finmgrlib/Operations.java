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
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.List;

@Service
public class Operations {

    public static MonetaryAmount currentACB(List<InvestmentTransaction> transactions) {

        // TODO: Implement base currency + auto conversion
        CurrencyUnit currency = Monetary.getCurrency(transactions.get(0).getCurrency().getValue());

        MonetaryAmount acb = Money.of(0, currency);
        BigDecimal quantity = new BigDecimal("0");

        for (InvestmentTransaction transaction : transactions) {

            switch (transaction.getAction()) {

                // ACB is summed with net amount of purchases
                case Reinvest:
                case Buy:
                    acb = acb.add(transaction.getNetAmount());
                    quantity = transaction.getQuantity().getValue().add(quantity);

                    break;

                // ACB per share remains constant during sales.
                case Sell:
                    MonetaryAmount acbPerShare = acb.divide(quantity);
                    quantity = transaction.getQuantity().getValue().add(quantity);

                    acb = acbPerShare.multiply(quantity);

                    break;

                // Return of Capital reduces ACB. Capital Gains distribution increases ACB
                case Distribution:
                    MonetaryAmount addend = transaction.getCapitalGain().multiply(quantity).negate();
                    MonetaryAmount subtrahend = transaction.getReturnOfCapital().multiply(quantity).negate();

                    acb = acb.add(addend).subtract(subtrahend);

                    break;

                default:
                    break;
            }

            if (quantity.equals(BigDecimal.ZERO)) {

                acb = Money.of(0, currency);
            }
        }

        if (quantity.equals(BigDecimal.ZERO)) {

            return acb;
        }

        return acb.divide(quantity).negate();
    }
}
