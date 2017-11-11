package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.DividendSQLRepository;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.stocklist.web.CachedDataAPI;
import org.cerion.stocklist.web.RepositoryCachedAPI;
import org.cerion.stocklist.web.YahooFinance;

public class Injection {

    public static CachedDataAPI getAPI(Context context) {
        context = context.getApplicationContext();
        return new RepositoryCachedAPI(
                YahooFinance.getInstance(),
                new PriceListSQLRepository(context),
                new DividendSQLRepository(context));
    }

    public static PositionRepository getPositionRepository(Context context) {
        return new PositionRepository(context.getApplicationContext());
    }
}
