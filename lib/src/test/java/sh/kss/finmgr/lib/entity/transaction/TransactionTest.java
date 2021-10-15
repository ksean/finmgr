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
package sh.kss.finmgr.lib.entity.transaction;


import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import sh.kss.finmgr.lib.FinmgrTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A subset of finmgr tests that validate the state of a transaction object
 *
 */
public abstract class TransactionTest extends FinmgrTest {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTest.class);

    /**
     * Assert that the input object when validated against the input validator returns the expected list of errors
     *
     * @param validator the validator for object validation
     * @param object the object to be validated
     * @param expectedErrors the list of errors expected to be produced
     */
    void assertHasErrors(Validator validator, Object object, ListMultimap<String, String> expectedErrors) {

        LOG.debug(String.format("assertHasErrors on object: %s", object.toString()));

        // Create the validator result object
        Errors actualErrors = new BeanPropertyBindingResult(object, object.getClass().toString());

        // Feed it into the validator with the input object
        validator.validate(object, actualErrors);

        // Assert that it has the specified errors
        assertHasFieldErrors(expectedErrors, actualErrors);
    }


    /**
     * Assert that the expected errors matches the input errors
     *
     * @param expectedErrors expected errors
     * @param actualErrors actual errors
     */
    private void assertHasFieldErrors(ListMultimap<String, String> expectedErrors, Errors actualErrors) {

        // Keep a count
        int expectedErrorSum = 0;

        // Loop through the expected field names to produce errors
        for (String fieldName : expectedErrors.keySet()) {

            // Get all error codes for that field
            List<String> expectedFieldErrors = expectedErrors.get(fieldName);

            // Get the actual error codes for this field
            List<FieldError> actualFieldErrors = actualErrors.getFieldErrors(fieldName);

            // Assert that the error codes that are expected exist for the field
            assertFieldnameHasErrors(expectedFieldErrors, actualFieldErrors);

            // Add the number of errors for this field to the sum
            expectedErrorSum += expectedFieldErrors.size();
        }

        // The count of errors matches
        assertEquals(
            expectedErrorSum,
            actualErrors.getAllErrors().size()
        );
    }


    /**
     * All errors for a field must match the expected ones explicitly
     *
     * @param expectedFieldErrors expected field errors
     * @param actualFieldErrors actual field errors produced
     */
    private void assertFieldnameHasErrors(List<String> expectedFieldErrors, List<FieldError> actualFieldErrors) {

        // Get the expected field error count size
        int expectedErrorCount = expectedFieldErrors.size();

        // Assert validation generated the expected number of field errors
        assertEquals(
            expectedErrorCount,
            actualFieldErrors.size()
        );

        // Loop through the expected error counts and check that an actual error was produced with that code
        for (int i = 0; i < expectedErrorCount; i++) {

            // Get the error codes for the field
            String[] actualFieldError = actualFieldErrors.get(i).getCodes();

            // Error codes must exist
            assertNotNull(actualFieldError);

            // Has at least one error code
            assertTrue(actualFieldError.length > 0);

            // The usefuel code is always last in the array
            String lastError = actualFieldError[actualFieldError.length - 1];

            // Must have the expected code
            assertEquals(
                expectedFieldErrors.get(i),
                lastError
            );
        }
    }
}
