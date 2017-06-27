package org.cerion.stockcharts.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stockcharts.database.Tables;
import org.cerion.stocklist.model.Interval;

public class MasterRepository extends SQLiteRepositoryBase {

    private static final String TAG = MasterRepository.class.getSimpleName();

    public MasterRepository(Context context) {
        super(context);
    }

    public long clearCache() {
        long s1 = getDbSize();

        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.DAILY));
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.WEEKLY));
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.MONTHLY));
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DAILY);
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY);
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY);
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DIVIDENDS);
        deleteAll(Tables.Dividends.TABLE_NAME);

        optimize();
        long s2 = getDbSize();
        Log.d(TAG, "Reduced size by " + (s1 - s2) + " bytes, total size is " + s2);

        log();

        return (s1 - s2);
    }
}
