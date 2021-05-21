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

import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;

/**
 * A quantity simply wraps a BigDecimal and exposes some commonly used statics values
 *
 */
@With
@Value
public class Quantity {

    public static final Quantity ZERO = new Quantity(BigDecimal.ZERO);
    public static final Quantity ONE = new Quantity(BigDecimal.ONE);
    public static final Quantity TEN = new Quantity(BigDecimal.TEN);
    public static final Quantity HUNDRED = new Quantity(new BigDecimal(100));

    @NonNull BigDecimal value;
}
