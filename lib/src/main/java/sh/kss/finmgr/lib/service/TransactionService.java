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
package sh.kss.finmgr.lib.service;

import sh.kss.finmgr.lib.entity.AccountType;
import sh.kss.finmgr.lib.entity.Portfolio;
import sh.kss.finmgr.lib.entity.Security;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.Map;

public interface TransactionService {

    MonetaryAmount getACB(Portfolio portfolio, AccountType accountType, Security security);
    Map<String, MonetaryAmount> getHistoricalPrices(Portfolio portfolio, LocalDate localDate);
}
