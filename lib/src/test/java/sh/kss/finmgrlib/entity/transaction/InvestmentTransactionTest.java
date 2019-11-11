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
package sh.kss.finmgrlib.entity.transaction;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import sh.kss.finmgrlib.entity.Currency;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.List;

import static org.junit.Assert.assertEquals;


@SpringBootTest
@RunWith(SpringRunner.class)
public class InvestmentTransactionTest extends TransactionTest {

    private final InvestmentTransactionValidator VALIDATOR = new InvestmentTransactionValidator();

    private final List<InvestmentTransaction> TEST_TRANSACTIONS = ImmutableList.of(
        BUY_VTI,
        BUY_VTI_HIGHER_PRICE,
        SELL_VTI,
        SELL_VTI_LOWER_PRICE,
        VTI_DIVIDEND,
        VTI_RETURN_OF_CAPITAL,
        VTI_CAPITAL_GAIN
    );


    @Test
    public void validTransactionFixtures() {

        TEST_TRANSACTIONS.forEach(fixture -> {

            // Run Validation
            Errors errors = new BeanPropertyBindingResult(fixture, fixture.toString());
            VALIDATOR.validate(fixture, errors);

            // Assertions
            assertEquals(
                0,
                errors.getAllErrors().size()
            );
        });
    }


    @Test
    public void inconsistentCurrencyAndNetAmountTest() {
        // Setup
        CurrencyUnit usd = Monetary.getCurrency("USD");

        InvestmentTransaction inconsistentTransaction = BUY_VTI
            .withGrossAmount(Money.of(-10_001, Currency.UNIT_CAD))
            .withNetAmount(Money.of(-9_995, usd));

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("netAmount", "currencyInconsistent");
        expectedErrors.put("netAmount", "netAmountSum");
        expectedErrors.put("grossAmount", "grossAmountProduct");

        assertHasErrors(VALIDATOR, inconsistentTransaction, expectedErrors);
    }


    @Test
    public void sameSettlementDateTransactionTest() {

        InvestmentTransaction settledSameDay = BUY_VTI
            .withSettlementDate(BUY_VTI.getTransactionDate());

        // Validation
        Errors errors = new BeanPropertyBindingResult(settledSameDay, "settledSameDay");
        VALIDATOR.validate(settledSameDay, errors);

        // Assertions
        assertEquals(
            0,
            errors.getAllErrors().size()
        );
    }


    @Test
    public void settledBeforeTransactionDateTest() {

        InvestmentTransaction badSettlementDateTransaction = BUY_VTI
            .withSettlementDate(BUY_VTI.getTransactionDate().minusDays(1));

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("settlementDate", "settledBeforeTransaction");

        assertHasErrors(VALIDATOR, badSettlementDateTransaction, expectedErrors);
    }


    @Test
    public void invalidGrossAmountTest() {

        InvestmentTransaction invalidGrossAmountTransaction = BUY_VTI
            .withGrossAmount(Money.of(-10_001, Currency.UNIT_CAD));

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("grossAmount", "grossAmountProduct");
        expectedErrors.put("netAmount", "netAmountSum");

        assertHasErrors(VALIDATOR, invalidGrossAmountTransaction, expectedErrors);
    }


    @Test
    public void invalidNetAmountTest() {

        InvestmentTransaction invalidNetAmountTransaction = BUY_VTI
            .withNetAmount(Money.of(-10_004, Currency.UNIT_CAD));

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("netAmount", "netAmountSum");

        assertHasErrors(VALIDATOR, invalidNetAmountTransaction, expectedErrors);
    }


    @Test
    public void invalidCommissionTest() {

        InvestmentTransaction invalidNetAmountTransaction = BUY_VTI
            .withCommission(Money.of(5, Currency.UNIT_CAD));

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("commission", "commissionPositive");
        expectedErrors.put("netAmount", "netAmountSum");

        assertHasErrors(VALIDATOR, invalidNetAmountTransaction, expectedErrors);
    }
}
