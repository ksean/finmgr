/*
    finmgr - A financial transaction framework
    Copyright (C) 2018 Kennedy Software Solutions Inc.

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
import com.google.common.collect.ListMultimap;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import sh.kss.finmgrlib.entity.*;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InvestmentTransactionTest extends TransactionTest {

    private final InvestmentTransactionValidator VALIDATOR = new InvestmentTransactionValidator();

    @Test
    public void validBuyTransaction() {

        // Validation
        Errors errors = new BeanPropertyBindingResult(BUY_VTI, "validBuyTransaction");
        VALIDATOR.validate(BUY_VTI, errors);

        // Assertions
        assertEquals(
            0,
            errors.getAllErrors().size()
        );
    }

    @Test
    public void validSellTransaction() {

        // Validation
        Errors errors = new BeanPropertyBindingResult(SELL_VTI, "validSellTransaction");
        VALIDATOR.validate(SELL_VTI, errors);

        // Assertions
        assertEquals(
            0,
            errors.getAllErrors().size()
        );
    }

    @Test
    public void inconsistentCurrencyAndNetAmountTest() {
        // Setup
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
            Money.of(9_995, usd), // should be -CAD$10,005
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
            Money.of(0, cad),
            Money.of(0, cad),
            new Currency(BASE_CURRENCY)
        );

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("netAmount", "currencyInconsistent");
        expectedErrors.put("netAmount", "netAmountSum");
        expectedErrors.put("grossAmount", "grossAmountProduct");

        assertHasErrors(VALIDATOR, inconsistentTransaction, expectedErrors);
    }



    @Test
    public void sameSettlementDateTransactionTest() {

        // Setup
        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction settledSameDay = new InvestmentTransaction(
            now, // Same transaction and settlement dates
            now,
            InvestmentAction.Buy,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(100)),
            Money.of(100, cad),
            Money.of(-10_000, cad),
            Money.of(-5, cad),
            Money.of(-10_005, cad),
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
            Money.of(0, cad),
            Money.of(0, cad),
            new Currency(BASE_CURRENCY)
        );

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

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction badSettlementDateTransaction = new InvestmentTransaction(
            now,
            now.minusDays(3), // Settled before transaction
            InvestmentAction.Buy,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(100)),
            Money.of(100, cad),
            Money.of(-10_000, cad),
            Money.of(-5, cad),
            Money.of(-10_005, cad),
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
            Money.of(0, cad),
            Money.of(0, cad),
            new Currency(BASE_CURRENCY)
        );

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("settlementDate", "settledBeforeTransaction");

        assertHasErrors(VALIDATOR, badSettlementDateTransaction, expectedErrors);
    }



    @Test
    public void invalidGrossAmountTest() {

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction invalidGrossAmountTransaction = new InvestmentTransaction(
            now,
            now.plusDays(3),
            InvestmentAction.Buy,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(100)),
            Money.of(100, cad),
            Money.of(-10_001, cad), // Should be -$CAD10,000
            Money.of(-5, cad),
            Money.of(-10_006, cad),
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
            Money.of(0, cad),
            Money.of(0, cad),
            new Currency(BASE_CURRENCY)
        );

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("grossAmount", "grossAmountProduct");

        assertHasErrors(VALIDATOR, invalidGrossAmountTransaction, expectedErrors);
    }

    @Test
    public void invalidNetAmountTest() {

        LocalDate now = LocalDate.now();
        CurrencyUnit cad = Monetary.getCurrency("CAD");

        InvestmentTransaction invalidNetAmountTransaction = new InvestmentTransaction(
            now,
            now.plusDays(3),
            InvestmentAction.Buy,
            new Symbol("VTI"),
            new Quantity(new BigDecimal(100)),
            Money.of(100, cad),
            Money.of(-10_000, cad),
            Money.of(-5, cad),
            Money.of(-10_004, cad), // Should be -$CAD10,005
            new Account("123-abc", "foo", AccountType.NON_REGISTERED),
            Money.of(0, cad),
            Money.of(0, cad),
            new Currency(BASE_CURRENCY)
        );

        // Assert spring validator errors
        ListMultimap<String, String> expectedErrors = ArrayListMultimap.create();
        expectedErrors.put("netAmount", "netAmountSum");

        assertHasErrors(VALIDATOR, invalidNetAmountTransaction, expectedErrors);
    }
}
