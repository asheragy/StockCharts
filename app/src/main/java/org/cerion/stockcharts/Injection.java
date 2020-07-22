package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.stocks.core.repository.CachedPriceListRepository;
import org.cerion.stocks.core.repository.DefaultPriceHistoryDates;
import org.cerion.stocks.core.web.CombinedDataAPI;
import org.cerion.stocks.core.web.DataAPI;
import org.cerion.stocks.core.web.clients.YahooFinance;

@Deprecated
public class Injection {

    private static DataAPI dataAPI = new CombinedDataAPI(BuildConfig.TIINGO_APIKEY);

    public static DataAPI getDataApi() {
        return dataAPI;
    }

    public static CachedPriceListRepository getPriceListRepository(Context context) {
        context = context.getApplicationContext();
        return new CachedPriceListRepository(
                new PriceListSQLRepository(context),
                YahooFinance.Companion.getInstance(),
                new DefaultPriceHistoryDates());
    }
}
