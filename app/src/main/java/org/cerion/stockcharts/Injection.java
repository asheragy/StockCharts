package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.CachedPriceListRepository;
import org.cerion.stockcharts.repository.DefaultPriceHistoryDates;
import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.marketdata.webclients.yahoo.YahooFinance;

@Deprecated
public class Injection {

    public static CachedPriceListRepository getPriceListRepository(Context context) {
        context = context.getApplicationContext();
        return new CachedPriceListRepository(
                new PriceListSQLRepository(context),
                YahooFinance.Companion.getInstance(),
                new DefaultPriceHistoryDates());
    }
}
