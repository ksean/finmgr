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
package sh.kss.finmgrlib.operation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import sh.kss.finmgrlib.data.MarketDataApi;
import sh.kss.finmgrlib.entity.Holding;

import javax.inject.Singleton;
import javax.money.MonetaryAmount;
import java.util.Map;

@Component
@Singleton
@AllArgsConstructor
public class NetPresentValue implements DailyOperation {

    private final MarketDataApi marketDataApi;

    @Override
    public Map<String, MonetaryAmount> process(Holding holding) {
        //TODO: implement
        return null;
    }
}
