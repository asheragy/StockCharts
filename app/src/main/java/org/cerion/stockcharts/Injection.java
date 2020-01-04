package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.stocks.core.repository.CachedPriceListRepository;
import org.cerion.stocks.core.web.CombinedDataAPI;
import org.cerion.stocks.core.web.DataAPI;
import org.cerion.stocks.core.web.clients.TDAmeritrade;

public class Injection {

    private static DataAPI dataAPI = new CombinedDataAPI(BuildConfig.TIINGO_APIKEY);

    public static DataAPI getDataApi() {
        return dataAPI;
    }

    public static CachedPriceListRepository getPriceListRepository(Context context) {
        context = context.getApplicationContext();
        return new CachedPriceListRepository(
                new PriceListSQLRepository(context),
                dataAPI);
    }

    public static TDAmeritrade getTD() {
        return new TDAmeritrade(BuildConfig.TD_CONSUMER_KEY, BuildConfig.TD_REDIRECT_URI);
    }
}
