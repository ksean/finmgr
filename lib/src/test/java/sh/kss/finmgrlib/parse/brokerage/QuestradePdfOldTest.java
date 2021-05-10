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
package sh.kss.finmgrlib.parse.brokerage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.entity.*;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.ParseTest;

import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test the questrade PDF parser
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class QuestradePdfOldTest extends ParseTest {

    /**
     * The text version of an example Questrade PDF document
     */
    @Value("classpath:questrade/2011-jan.txt")
    Resource resourceFile;

    private static final QuestradePdfOld QUESTRADE_PDF = new QuestradePdfOld();

    private static List<String> lines;

    private static final Logger LOG = LogManager.getLogger(QuestradePdfOldTest.class);


    /**
     * Sets up the tests by converting a resource test file into a list of strings
     */
    @Before
    public void setup() {

        LOG.info("Setting up tests");

        try {

            LOG.debug("Retrieving file fixture contents");
            lines = getLinesFromFile(resourceFile.getFile());

        }
        catch (IOException ioe) {

            LOG.debug(String.format("IOException trying to read %s", resourceFile.getFilename()));
            ioe.printStackTrace();
        }
    }


    /**
     * Questrade parser should match the test list of strings
     */
    @Test
    public void questradeTextMatchesTest() {

        assertTrue(QUESTRADE_PDF.isMatch(lines));
    }


    /**
     * The parser should return one transaction with a net amount of $CAD 1500
     */
    @Test
    public void oneTransactionTest() {

        // Parse the transactions
        List<InvestmentTransaction> transactions = QUESTRADE_PDF.parse(lines);

        // Fixtures
        final MonetaryAmount ZERO_CAD = Money.of(0, "CAD");

        // Expected transaction
        InvestmentTransaction expectedTransaction = InvestmentTransaction.builder()
            .transactionDate(LocalDate.of(2011,12,21))
            .settlementDate(LocalDate.of(2011,12,21))
            .action(InvestmentAction.Deposit)
            .account(new Account("UNKNOWN", "UNKNOWN", AccountType.NON_REGISTERED))
            .symbol(new Symbol("UNKNOWN"))
            .currency(Currency.CAD)
            .description("1234567827 CIBC DIR DEP")
            .price(ZERO_CAD)
            .grossAmount(ZERO_CAD)
            .commission(ZERO_CAD)
            .netAmount(Money.of(1500, "CAD"))
            .quantity(new Quantity(new BigDecimal(0)))
            .eligibleDividend(ZERO_CAD)
            .nonEligibleDividend(ZERO_CAD)
            .returnOfCapital(ZERO_CAD)
            .capitalGain(ZERO_CAD)
            .build();


        // Exactly 1 transaction is parsed from the input
        assertEquals(
            1,
            transactions.size()
        );

        // The transaction parsed matches the expected transaction defined explicitly above
        assertEquals(
            expectedTransaction,
            transactions.get(0)
        );
    }
}
