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


import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import sh.kss.finmgrlib.Operations;
import sh.kss.finmgrlib.entity.*;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InvestmentTransactionTest {

    @Autowired
    Operations operations;

    private final InvestmentTransactionValidator VALIDATOR = new InvestmentTransactionValidator();

    private final String BASE_CURRENCY = "CAD";

    // VALID buy and sell transactions
    private final InvestmentTransaction buyVTI = new InvestmentTransaction(
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

    private final InvestmentTransaction sellVTI = new InvestmentTransaction(
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

        // Validation
        Errors errors = new BeanPropertyBindingResult(inconsistentTransaction, "inconsistentTransaction");
        VALIDATOR.validate(inconsistentTransaction, errors);

        List<FieldError> netAmountErrors = errors.getFieldErrors("netAmount");
        FieldError netAmountCurrencyError = netAmountErrors.get(0);
        FieldError netAmountSumError = netAmountErrors.get(1);
        FieldError grossAmountProductError = errors.getFieldError("grossAmount");

        String[] netAmountCurrencyCodes = netAmountCurrencyError.getCodes();
        String[] netAmountSumCodes = netAmountSumError.getCodes();
        String[] grossAmountProductCodes = grossAmountProductError.getCodes();

        // Assertions
        assertEquals(
            3,
            errors.getAllErrors().size()
        );

        assertNotNull(netAmountErrors);
        assertNotNull(netAmountCurrencyError);
        assertNotNull(netAmountSumError);
        assertNotNull(grossAmountProductError);

        if (netAmountCurrencyCodes != null) {
            assertTrue(netAmountCurrencyCodes.length > 1);
            assertEquals(
                "currencyInconsistent",
                netAmountCurrencyCodes[netAmountCurrencyCodes.length - 1]
            );
        }
        if (netAmountSumCodes != null) {
            assertTrue(netAmountSumCodes.length > 1);
            assertEquals(
                "netAmountSum",
                netAmountSumCodes[netAmountSumCodes.length - 1]
            );
        }
        if (grossAmountProductCodes != null) {
            assertTrue(grossAmountProductCodes.length > 1);
            assertEquals(
                "grossAmountProduct",
                grossAmountProductCodes[grossAmountProductCodes.length - 1]
            );
        }

    }

    @Test
    public void consistentCurrencyTest() {

        // Validation
        Errors errors = new BeanPropertyBindingResult(buyVTI, "inconsistentTransaction");
        VALIDATOR.validate(buyVTI, errors);

        // Assertions
        assertEquals(
            0,
            errors.getAllErrors().size()
        );
    }

    @Test
    public void settledOnOrAfterTransactionDateTest() {

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

        // Validation
        Errors errors = new BeanPropertyBindingResult(badSettlementDateTransaction, "badSettlementDateTransaction");
        VALIDATOR.validate(badSettlementDateTransaction, errors);
        FieldError settlementDateError = errors.getFieldError("settlementDate");

        // Assertions
        assertEquals(
            1,
            errors.getAllErrors().size()
        );
        assertNotNull(settlementDateError);

        if (settlementDateError.getCodes() != null) {
            assertEquals(
                "settledBeforeTransaction",
                settlementDateError.getCodes()[settlementDateError.getCodes().length - 1]
            );
        }
    }

    @Test
    public void validGrossAmountTest() {

        // Validation
        Errors errors = new BeanPropertyBindingResult(sellVTI, "validGrossAmount");
        VALIDATOR.validate(sellVTI, errors);

        // Assertions
        assertEquals(
            0,
            errors.getAllErrors().size()
        );
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

        // Validation
        Errors errors = new BeanPropertyBindingResult(invalidGrossAmountTransaction, "invalidGrossAmountTransaction");
        VALIDATOR.validate(invalidGrossAmountTransaction, errors);
        FieldError grossAmountError = errors.getFieldError("grossAmount");

        // Assertions
        assertEquals(
            1,
            errors.getAllErrors().size()
        );
        assertNotNull(grossAmountError);

        if (grossAmountError.getCodes() != null) {
            assertEquals(
                "grossAmountProduct",
                grossAmountError.getCodes()[grossAmountError.getCodes().length - 1]
            );
        }
    }

    @Test
    public void validNetAmountTest() {

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

        // Validation
        Errors errors = new BeanPropertyBindingResult(invalidNetAmountTransaction, "invalidNetAmountTransaction");
        VALIDATOR.validate(invalidNetAmountTransaction, errors);
        FieldError netAmountError = errors.getFieldError("netAmount");

        // Assertions
        assertEquals(
            1,
            errors.getAllErrors().size()
        );
        assertNotNull(netAmountError);

        if (netAmountError.getCodes() != null) {
            assertEquals(
                "netAmountSum",
                netAmountError.getCodes()[netAmountError.getCodes().length - 1]
            );
        }
    }
}
