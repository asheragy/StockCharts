package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.stocks.core.repository.CachedPriceListRepository;
import org.cerion.stocks.core.repository.DefaultPriceHistoryDates;
import org.cerion.stocks.core.web.clients.YahooFinance;

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
