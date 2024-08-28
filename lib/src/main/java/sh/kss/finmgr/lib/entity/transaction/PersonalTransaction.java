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

import lombok.Builder;
import lombok.Value;
import lombok.With;
import sh.kss.finmgr.lib.entity.Account;
import sh.kss.finmgr.lib.entity.TransactionCategory;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.time.LocalDate;


/**
 * A personal transaction is one that happens in a personal account like a chequings account or a credit card
 *
 */
@Value
@With
@Builder(toBuilder = true)
public class PersonalTransaction {

    LocalDate transactionDate;
    LocalDate settlementDate;
    Account account;
    MonetaryAmount netAmount;
    CurrencyUnit currency;
    String description;

    TransactionCategory transactionCategory;
}
