package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.stocks.core.repository.CachedPriceListRepository;
import org.cerion.stocks.core.web.CombinedDataAPI;
import org.cerion.stocks.core.web.DataAPI;

public class Injection {

    private static DataAPI dataAPI = new CombinedDataAPI();

    public static DataAPI getDataApi() {
        return dataAPI;
    }

    public static CachedPriceListRepository getPriceListRepository(Context context) {
        context = context.getApplicationContext();
        return new CachedPriceListRepository(
                new PriceListSQLRepository(context),
                new CombinedDataAPI());
    }
}
