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


import com.google.common.collect.ListMultimap;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import sh.kss.finmgrlib.FinmgrTest;

import java.util.List;

import static org.junit.Assert.*;


@Service
public class TransactionTest extends FinmgrTest {


    void assertHasErrors(Validator validator, Object object, ListMultimap<String, String> expectedErrors) {

        Errors errors = new BeanPropertyBindingResult(object, object.getClass().toString());
        validator.validate(object, errors);

        assertHasFieldErrors(errors, expectedErrors);
    }


    private void assertHasFieldErrors(Errors errors, ListMultimap<String, String> expectedErrors) {

        int expectedErrorSum = 0;

        for (String fieldName : expectedErrors.keySet()) {
            List<String> codes = expectedErrors.get(fieldName);
            int numberOfErrorCodes = codes.size();

            if (numberOfErrorCodes > 1) {
                assertFieldHasMultiErrorCodes(errors, fieldName, codes);
            }
            else {
                assertFieldHasOneErrorCode(errors, fieldName, codes);
            }

            expectedErrorSum += numberOfErrorCodes;
        }

        // There is an error for each expected error
        assertEquals(
            expectedErrorSum,
            errors.getAllErrors().size()
        );
    }


    private void assertFieldHasOneErrorCode(Errors errors, String fieldName, List<String> code) {

        FieldError fieldError =  errors.getFieldError(fieldName);

        // Field error must exist
        assertNotNull(fieldError);

        String[] errorCodes = fieldError.getCodes();

        // Error codes must exist
        assertNotNull(errorCodes);

        // Has at least one error code
        assertTrue(errorCodes.length > 1);

        // The last error code matches expected
        assertEquals(
            code.get(0),
            errorCodes[errorCodes.length - 1]
        );
    }


    private void assertFieldHasMultiErrorCodes(Errors errors, String fieldName, List<String> codes) {

        List<FieldError> fieldErrors = errors.getFieldErrors(fieldName);

        int numberOfErrors = fieldErrors.size();

        // Assert validation generated the expected number of field errors
        assertEquals(
            codes.size(),
            numberOfErrors
        );

        for (int i = 0; i < numberOfErrors; i++) {

            String[] errorCodes = fieldErrors.get(i).getCodes();

            // Error codes must exist
            assertNotNull(errorCodes);

            // Has at least one error code
            assertTrue(errorCodes.length > 1);

            String lastError = errorCodes[errorCodes.length - 1];

            // Must have the expected code
            assertEquals(
                codes.get(i),
                lastError
            );
        }
    }
}
