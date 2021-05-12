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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgrlib.entity.Symbol;
import sh.kss.finmgrlib.map.CurrencyAndCountry;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class MarketWatchApi implements MarketDataApi {

    // Log manager
    private static final Logger LOG = LoggerFactory.getLogger(MarketWatchApi.class);

    DateTimeFormatter marketWatchDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    public MonetaryAmount getClosingPrice(Symbol symbol, LocalDate date, CurrencyUnit currency) {

        LOG.debug("called getClosingPrice()");

        String formattedStartDate = date.minusDays(5).format(marketWatchDateFormat);
        String formattedDate = date.format(marketWatchDateFormat);
        String countryCode = CurrencyAndCountry.currencyToCountryAlpha2.get(currency);

        try {
            Document doc = Jsoup.connect(String.format("https://www.marketwatch.com/investing/fund/%s/download-data?startDate=%s&endDate=%s&countryCode=%s", symbol.getValue(), formattedStartDate, formattedDate, countryCode)).get();
            LOG.debug(doc.title());
            LOG.info(doc.selectFirst("div.tab__pane:nth-child(1) > mw-downloaddata:nth-child(1) > div:nth-child(2) > div:nth-child(1) > table:nth-child(1) > tbody:nth-child(2) > tr:nth-child(1) > td:nth-child(5) > div:nth-child(1)").html());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<LocalDate, MonetaryAmount> getClosingPrices(Symbol symbol, List<LocalDate> dates, CurrencyUnit currency) {

        LOG.debug("called getClosingPrices()");

        return null;
    }

}
