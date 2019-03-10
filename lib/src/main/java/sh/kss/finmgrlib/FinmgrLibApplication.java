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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;
import sh.kss.finmgrlib.parse.PdfParser;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class FinmgrLibApplication {

    public static void main(String[] args) {

        SpringApplication.run(FinmgrLibApplication.class, args);

        // Temporarily test any pdf document in classpath
        List<InvestmentTransaction> transactions = PdfParser.fromPath("/home/s/dev/java/finmgr/lib/src/main/resources/");

        Collections.sort(transactions);

        for (InvestmentTransaction transaction : transactions) {
            System.out.println(transaction);
        }

    }
}
