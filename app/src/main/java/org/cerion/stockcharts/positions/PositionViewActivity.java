package org.cerion.stockcharts.positions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.IYahooFinance;
import org.cerion.stocklist.web.YahooFinance;

import java.text.DecimalFormat;
import java.util.Date;

public class PositionViewActivity extends AppCompatActivity {

    //public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_POSITION_SYMBOL = "position_symbol";
    public static final String EXTRA_POSITION_DATE = "position_date";
    public static final String EXTRA_POSITION_COUNT = "position_count";
    public static final String EXTRA_POSITION_PRICE = "position_price";
    public static final String EXTRA_POSITION_DIV = "position_dividends_reinvested";

    private Position mPosition;

    private static DecimalFormat df = new DecimalFormat("0.00");
    private StockDB mDb;
    private IYahooFinance mAPI = new YahooFinance();
    private StockDataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_view);

        mDb = StockDB.getInstance(this);
        mDataManager = new StockDataManager(this);

        String symbol = (String) getIntent().getSerializableExtra(EXTRA_POSITION_SYMBOL);
        Date date = (Date) getIntent().getSerializableExtra(EXTRA_POSITION_DATE);
        double count = (double) getIntent().getSerializableExtra(EXTRA_POSITION_COUNT);
        double price = (double) getIntent().getSerializableExtra(EXTRA_POSITION_PRICE);
        boolean div = (boolean) getIntent().getSerializableExtra(EXTRA_POSITION_DIV);

        mPosition = new Position(symbol,count,price,date, div);

        update();

        new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                final String symbol = mPosition.getSymbol();

                // Add dividends to position
                mPosition.addDividends(mDb.getDividends(symbol));
                if(mPosition.IsDividendsReinvested())
                    mPosition.setPriceHistory(mDataManager.getLatestPrices(symbol, Interval.DAILY));

                // Get most recent quote
                if(mPosition.getCurrPrice() == 0) {
                    Quote q = mAPI.getQuote(symbol);
                    mPosition.setQuote(q);
                }
            }

            @Override
            public void onFinish() {
                onQuoteUpdated();
            }
        }).execute();
    }

    private void update() {
        setText(R.id.symbol, mPosition.getSymbol());
        setText(R.id.count, Utils.getDecimalFormat3(mPosition.getCount()) );
        setText(R.id.purchase_price, df.format(mPosition.getOrigPrice()) );
        setText(R.id.purchase_date, Utils.dateFormatLong.format(mPosition.getDate()) );
        setText(R.id.purchase_value, "$" + df.format(mPosition.getOrigValue()) );

        setText(R.id.dividends_earned, df.format(mPosition.getDividendProfit()));
        setText(R.id.dividends_reinvested, mPosition.IsDividendsReinvested() ? "Yes" : "No");

        // If dividends reinvested hide all the dividend related fields, its treated as part of the stock price in this case
        if(mPosition.IsDividendsReinvested()) {
            hideField(R.id.last_dividend);
            hideField(R.id.next_dividend);
            hideField(R.id.last_dividend_date);

            //Labels
            hideField(R.id.last_dividend_label);
            hideField(R.id.next_dividend_label);
            hideField(R.id.last_dividend_date_label);

            hideRow(R.id.total_percent_change);
            hideRow(R.id.profit_with_dividends);
            hideRow(R.id.dividends_earned);
        } else {
            hideRow(R.id.current_count);
        }
    }

    private void onQuoteUpdated() {
        setText(R.id.current_price, df.format(mPosition.getCurrPrice()));
        setText(R.id.current_value, "$" + df.format(mPosition.getCurrValue()));

        setText(R.id.profit, "$" + df.format(mPosition.getProfit()));
        setText(R.id.price_percent_change, df.format(mPosition.getPercentChanged()) + "%");

        if(mPosition.getDividendProfit() != 0) {
            setText(R.id.total_percent_change, df.format(mPosition.getPercentChangedWithDividends()) + "%");
            setText(R.id.profit_with_dividends, df.format(mPosition.getProfit() + mPosition.getDividendProfit()));
        }

        //Dividends
        if(mPosition.IsDividendsReinvested()) {
            // TODO if dividends-Reinvested add new field for share count since that will have increased
            setText(R.id.current_count, Utils.getDecimalFormat3(mPosition.getCurrCount()));
        }
        else {
            setText(R.id.last_dividend, df.format(mPosition.getLastDividendPaid()));
            if (mPosition.getLastDividendDate() != null) {
                setText(R.id.last_dividend_date, Utils.dateFormatLong.format(mPosition.getLastDividendDate()));
                setText(R.id.next_dividend, Utils.dateFormatLong.format(mPosition.getNextDividendEstimate()));
            }
        }


    }

    private void setText(int id, String text) {
        ((TextView)findViewById(id)).setText(text);
    }

    private void hideField(int id) {
        findViewById(id).setVisibility(View.GONE);
    }

    private void hideRow(int id) {
        View v = findViewById(id);
        ViewGroup parent = (ViewGroup)v.getParent();
        int index = parent.indexOfChild(v);
        int labelIndex = index - 1;

        v.setVisibility(View.GONE);
        parent.getChildAt(labelIndex).setVisibility(View.GONE);
    }

}
