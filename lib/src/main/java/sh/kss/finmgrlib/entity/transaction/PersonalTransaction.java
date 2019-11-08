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

import lombok.Builder;
import lombok.Value;
import lombok.With;
import sh.kss.finmgrlib.entity.Account;
import sh.kss.finmgrlib.entity.Currency;
import sh.kss.finmgrlib.entity.TransactionCategory;

import javax.money.MonetaryAmount;
import java.time.LocalDate;


@Value
@With
@Builder(toBuilder = true)
public class PersonalTransaction {

    // TODO: Immutable inheritance with builder pattern for member fields:
    LocalDate transactionDate;
    LocalDate settlementDate;
    Account account;
    MonetaryAmount netAmount;
    Currency currency;
    String description;

    TransactionCategory transactionCategory;
}
