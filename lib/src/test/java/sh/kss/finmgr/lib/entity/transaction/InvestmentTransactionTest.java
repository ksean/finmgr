/*
    finmgr - A financial transaction framework
    Copyright (C) 2024 Kennedy Software Solutions Inc.

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
package sh.kss.finmgr.lib.entity.transaction;


import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the validity of investment transaction objects
 *
 */
@SpringBootTest
public class InvestmentTransactionTest extends TransactionTest {

    private final InvestmentTransactionValidator VALIDATOR = new InvestmentTransactionValidator();

    private final List<InvestmentTransaction> TEST_TRANSACTIONS = List.of(
        BUY_VTI,
        BUY_VTI_HIGHER_PRICE,
        SELL_VTI,
        SELL_VTI_LOWER_PRICE,
        VTI_DIVIDEND,
        VTI_RETURN_OF_CAPITAL,
        VTI_CAPITAL_GAIN
    );

    private static final Logger LOG = LoggerFactory.getLogger(InvestmentTransactionTest.class);


    /**
     * Asset that all transactions fixtures are valid
     *
     */
    @Test
    public void validTransactionFixtures() {

        LOG.debug("Validate all test fixture transactions");

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


    /**
     * Bad math for gross amounts and inconsistent currencies are detected
     *
     */
    @Test
    public void inconsistentCurrencyAndNetAmountTest() {

        InvestmentTransaction inconsistentTransaction = BUY_VTI
            .withGrossAmount(Money.of(-10_001, USD))
            .withNetAmount(Money.of(-9_995, CAD));

        // Assert spring validator errors
        Map<String, List<String>> expectedErrors = Map.of(
            "netAmount", List.of("currencyInconsistent", "netAmountSum"),
            "grossAmount", List.of("grossAmountProduct")
        );

        assertHasErrors(VALIDATOR, inconsistentTransaction, expectedErrors);
    }


    /**
     * Transactions may settle on the same day
     *
     */
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


    /**
     * Transactions may not settle before the transaction date
     *
     */
    @Test
    public void settledBeforeTransactionDateTest() {

        InvestmentTransaction badSettlementDateTransaction = BUY_VTI
            .withSettlementDate(BUY_VTI.getTransactionDate().minusDays(1));

        // Assert spring validator errors
        Map<String, List<String>> expectedErrors = Map.of(
            "settlementDate", List.of("settledBeforeTransaction")
        );

        assertHasErrors(VALIDATOR, badSettlementDateTransaction, expectedErrors);
    }


    /**
     * Gross amount bad math produces gross and net amount errors
     *
     */
    @Test
    public void invalidGrossAmountTest() {

        InvestmentTransaction invalidGrossAmountTransaction = BUY_VTI
            .withGrossAmount(Money.of(-10_001, USD));

        // Assert spring validator errors
        Map<String, List<String>> expectedErrors = Map.of(
            "grossAmount", List.of("grossAmountProduct"),
            "netAmount", List.of("netAmountSum")
        );

        assertHasErrors(VALIDATOR, invalidGrossAmountTransaction, expectedErrors);
    }


    /**
     * Net amount must sum to the net of transaction sum and expenses
     *
     */
    @Test
    public void invalidNetAmountTest() {

        InvestmentTransaction invalidNetAmountTransaction = BUY_VTI
            .withNetAmount(Money.of(-10_004, USD));

        // Assert spring validator errors
        Map<String, List<String>> expectedErrors = Map.of(
            "netAmount", List.of("netAmountSum")
        );

        assertHasErrors(VALIDATOR, invalidNetAmountTransaction, expectedErrors);
    }


    /**
     * Commission must be a negative amount
     *
     */
    @Test
    public void invalidCommissionTest() {

        InvestmentTransaction invalidNetAmountTransaction = BUY_VTI
            .withCommission(Money.of(5, USD));

        // Assert spring validator errors
        Map<String, List<String>> expectedErrors = Map.of(
            "commission", List.of("commissionPositive"),
            "netAmount", List.of("netAmountSum")
        );

        assertHasErrors(VALIDATOR, invalidNetAmountTransaction, expectedErrors);
    }
}
