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
package sh.kss.finmgrlib.data;

import sh.kss.finmgrlib.entity.Symbol;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 *
 */
public interface MarketDataApi {

    Optional<MonetaryAmount> getClosingPrice(Symbol symbol, LocalDate date, CurrencyUnit currency);

    Map<LocalDate, MonetaryAmount> getClosingPrices(Symbol symbol, List<LocalDate> dates, CurrencyUnit currency);


}
