package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.DividendSQLRepository;
import org.cerion.stockcharts.repository.PositionWithDividendsRepository;
import org.cerion.stockcharts.repository.PriceListSQLRepository;
import org.cerion.stocks.core.repository.CachedPriceListRepository;
import org.cerion.stocks.core.web.CachedDataAPI;
import org.cerion.stocks.core.web.CombinedDataAPI;
import org.cerion.stocks.core.web.RepositoryCachedAPI;

public class Injection {

    @Deprecated()
    public static CachedDataAPI getAPI(Context context) {
        context = context.getApplicationContext();
        return new RepositoryCachedAPI(
                new CombinedDataAPI(),
                new DividendSQLRepository(context));
    }

    public static CachedPriceListRepository getPriceListRepository(Context context) {
        context = context.getApplicationContext();
        return new CachedPriceListRepository(
                new PriceListSQLRepository(context),
                new CombinedDataAPI());
    }

    public static PositionWithDividendsRepository getPositionRepository(Context context) {
        return new PositionWithDividendsRepository(context.getApplicationContext());
    }
}
