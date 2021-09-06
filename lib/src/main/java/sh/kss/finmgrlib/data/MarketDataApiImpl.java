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

import org.javamoney.moneta.Money;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sh.kss.finmgrlib.entity.Security;
import sh.kss.finmgrlib.map.CurrencyAndCountry;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 *
 *
 */
@Component
public class MarketDataApiImpl implements MarketDataApi {

    // Log manager
    private static final Logger LOG = LoggerFactory.getLogger(MarketDataApiImpl.class);

    private static final DateTimeFormatter MARKET_WATCH_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String MARKET_WATCH_PRICE_LOOKUP = "div.tab__pane:nth-child(1) > mw-downloaddata:nth-child(1) > div:nth-child(2) > div:nth-child(1) > table:nth-child(1) > tbody:nth-child(2) > tr:nth-child(1) > td:nth-child(5) > div:nth-child(1)";
    private static final String MARKET_WATCH_URL = "https://www.marketwatch.com/investing/fund/%s/download-data?startDate=%s&endDate=%s&countryCode=%s";

    @Override
    public Optional<MonetaryAmount> findClosingPrice(Security security, LocalDate date) {

        CurrencyUnit currency = security.getCurrency();
        LOG.debug(String.format("called getClosingPrice(%s, %s, %s)", security.getValue(), date, currency));

        // You can't get a closing price for a future date
        checkArgument(date.isBefore(LocalDate.now()));

        // Ensure our date range will get us the latest business day closing price
        String formattedStartDate = date.minusDays(5).format(MARKET_WATCH_DATE_FORMAT);
        String formattedTargetDate = date.format(MARKET_WATCH_DATE_FORMAT);
        String countryCode = CurrencyAndCountry.currencyToCountryAlpha2.get(currency);
        String connectionUrl = String.format(MARKET_WATCH_URL, security.getValue(), formattedStartDate, formattedTargetDate, countryCode);

        try {

            Document doc = Jsoup.connect(connectionUrl).get();

            return Optional.of(
                Money.parse(currency.getCurrencyCode() + " " + doc.selectFirst(MARKET_WATCH_PRICE_LOOKUP).html()));

        } catch (IOException e) {

            LOG.error(String.format("IOException when trying to connect to url: %s", connectionUrl));
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Map<LocalDate, MonetaryAmount> getClosingPrices(Security security, List<LocalDate> dates) {

        LOG.debug("called getClosingPrices()");

        Map<LocalDate, MonetaryAmount> closingPrices = new HashMap<>();

        for (LocalDate date : dates) {

            findClosingPrice(security, date)
                .ifPresent(p -> closingPrices.put(date, p));
        }

        return closingPrices;
    }
}
