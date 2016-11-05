package org.cerion.stockcharts;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stockcharts.database.StockDBOpenHelper.Prices;
import org.cerion.stockcharts.database.StockDataProvider;
import org.cerion.stocklist.Enums;

import java.util.Date;

public class PriceListActivity extends AppCompatActivity {

    public static final String SYMBOL_EXTRA = "symbol";
    private String mSymbol;
    private PriceListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.price_list_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSymbol = getIntent().getStringExtra(SYMBOL_EXTRA);

        //StockDataStore mDb = StockDB.getInstance(this);
        //mDb.deletePrices(mSymbol, Enums.Interval.MONTHLY);

        mAdapter = new PriceListAdapter(this);
        ListView lv = (ListView)findViewById(android.R.id.list);
        lv.setAdapter(mAdapter);

        ((TextView)toolbar.findViewById(R.id.title)).setText(mSymbol);

        //Swipe Refresh
        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //Show swipe refresh progress while loading
        mSwipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                if(mAdapter.getCount() == 0)
                    refresh();
            }
        });
    }

    private void refresh()
    {
        mSwipeRefresh.setRefreshing(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                StockDataManager du = new StockDataManager(PriceListActivity.this);
                du.updatePrices(mSymbol, Enums.Interval.MONTHLY);
            }

            @Override
            public void onFinish() {
                mAdapter.refresh();
                mSwipeRefresh.setRefreshing(false);
            }
        });

        task.execute();
    }

    private class PriceListAdapter extends CursorAdapter {

        public PriceListAdapter(Context context) {
            super(context, null, 0);
            swapCursor(newCursor());
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.price_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            long date = cursor.getLong(cursor.getColumnIndex(Prices._DATE));
            Date d = new Date(date);

            ((TextView)view.findViewById(R.id.date)).setText(Utils.dateFormatShort.format(d));
            setDouble(view, R.id.open, cursor,  Prices._OPEN);
            setDouble(view, R.id.close, cursor, Prices._CLOSE);
            setDouble(view, R.id.high, cursor,  Prices._HIGH);
            setDouble(view, R.id.low, cursor,   Prices._LOW);
            ((TextView)view.findViewById(R.id.volume)).setText(cursor.getString(cursor.getColumnIndex(Prices._VOLUME)));
        }

        public void setDouble(View v, int id, Cursor c, String column) {
            double n = c.getDouble(c.getColumnIndex(column));
            ((TextView)v.findViewById(id)).setText( Utils.decimalFormat.format(n) );
        }

        public void refresh() {
            changeCursor(newCursor());
        }

        private Cursor newCursor() {
            return getContentResolver().query(
                    StockDataProvider.CONTENT_URI_PRICES.buildUpon().appendPath(mSymbol).build(),
                    Prices.ALL_COLUMNS,
                    null, null, null, null);
        }
    }
}
