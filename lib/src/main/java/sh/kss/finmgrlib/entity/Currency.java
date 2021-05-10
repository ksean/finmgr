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
package sh.kss.finmgrlib.entity;

import lombok.Value;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

/**
 *
 *
 */
@Value
public class Currency {

    public static final CurrencyUnit UNIT_CAD = Monetary.getCurrency("CAD");
    public static final CurrencyUnit UNIT_USD = Monetary.getCurrency("USD");
    public static final Currency CAD = new Currency("CAD");
    public static final Currency USD = new Currency("USD");


    String value;
}
