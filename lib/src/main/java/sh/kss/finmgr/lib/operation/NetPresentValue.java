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
package sh.kss.finmgr.lib.operation;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.kss.finmgr.lib.data.MarketDataApi;
import sh.kss.finmgr.lib.entity.Holding;
import sh.kss.finmgr.lib.entity.Security;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NetPresentValue implements DailyOperation {

    private MarketDataApi marketDataApi;

    @Autowired
    public void setMarketDataApi(MarketDataApi marketDataApi) {
        this.marketDataApi = marketDataApi;
    }

    @Override
    public Map<Security, MonetaryAmount> process(Holding holding, LocalDate date) {
        Set<Security> securities = holding.getQuantities().keySet();
        return securities.stream()
            .collect(Collectors.toMap(Function.identity(), s -> marketDataApi.findClosingPrice(s, date).orElse(Money.of(0, s.getCurrency())).multiply(holding.getQuantities().get(s).getValue())));
    }

    @Override
    public String getName() {
        return "NPV";
    }
}
