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
package sh.kss.finmgrlib.parse.brokerage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import sh.kss.finmgrlib.parse.ParseTest;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@SpringBootTest
@RunWith(SpringRunner.class)
public class QuestradeTest extends ParseTest {

    @Value("classpath:questrade/2011-jan.txt")
    Resource resourceFile;

    private final Questrade QUESTRADE = new Questrade();

    private List<String> lines;


    @Before
    public void setup() {

        try {

            lines = getLinesFromFile(resourceFile.getFile());

        }
        catch (IOException ioe) {

            ioe.printStackTrace();
        }
    }


    @Test
    public void questradeTextMatchesTest() {

        assertTrue(QUESTRADE.isMatch(lines));
    }


    @Test
    public void noTransactionsTest() {

        assertEquals(
            1,
            QUESTRADE.parse(lines).size()
        );
    }
}
