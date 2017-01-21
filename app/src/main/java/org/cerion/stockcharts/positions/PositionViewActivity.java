package org.cerion.stockcharts.positions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.YahooFinance;

import java.text.DecimalFormat;

public class PositionViewActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";
    private Position mPosition;

    private static DecimalFormat df = new DecimalFormat("0.00");
    private StockDB mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_view);

        mDb = StockDB.getInstance(this);

        mPosition = (Position) getIntent().getSerializableExtra(EXTRA_POSITION);
        update();

        new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                // Add dividends to position
                mPosition.addDividends(mDb.getDividends(mPosition.getSymbol()));

                // Get most recent quote
                if(mPosition.getCurrPrice() == 0) {
                    Quote q = YahooFinance.getQuote(mPosition.getSymbol());
                    mPosition.setCurrPrice(q.lastTrade);
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
        }
    }

    private void onQuoteUpdated() {
        setText(R.id.current_price, df.format(mPosition.getCurrPrice()));
        setText(R.id.current_value, "$" + df.format(mPosition.getCurrValue()));

        setText(R.id.profit, df.format(mPosition.getProfit()));
        setText(R.id.price_percent_change, df.format(mPosition.getPercentChanged()) + "%");

        setText(R.id.total_percent_change, df.format(mPosition.getPercentChangedWithDividends()) + "%");
        setText(R.id.profit_with_dividends, df.format(mPosition.getProfit() + mPosition.getDividendProfit()));

        // TODO, hide if non-combined position, only useful there
        setText(R.id.break_even_price, df.format(mPosition.getOrigPrice()));
        // Calculate based on dividends earned X = (original*count - dividends) / count
        double value = mPosition.getOrigValue() - mPosition.getDividendProfit();
        setText(R.id.break_even_dividends, df.format(value / mPosition.getCount()));

        //Dividends
        setText(R.id.last_dividend, df.format(mPosition.getLastDividendPaid()));
        if(mPosition.getLastDividendDate() != null) {
            setText(R.id.last_dividend_date, Utils.dateFormatLong.format(mPosition.getLastDividendDate()));
            setText(R.id.next_dividend, Utils.dateFormatLong.format(mPosition.getNextDividendEstimate()));
        }

    }

    private void setText(int id, String text) {
        ((TextView)findViewById(id)).setText(text);
    }

    private void hideField(int id) {
        findViewById(id).setVisibility(View.GONE);
    }

}
