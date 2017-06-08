package org.cerion.stockcharts.positions;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.RetainFragment;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.viewmodel.PositionViewModel;
import org.cerion.stocklist.model.Position;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class PositionViewActivity extends AppCompatActivity implements Observer {

    public static final String EXTRA_POSITION_SYMBOL = "position_symbol";
    public static final String EXTRA_POSITION_DATE = "position_date";
    public static final String EXTRA_POSITION_COUNT = "position_count";
    public static final String EXTRA_POSITION_PRICE = "position_price";
    public static final String EXTRA_POSITION_DIV = "position_dividends_reinvested";
    private static final String TAG = PositionViewActivity.class.getSimpleName();
    private static final String RETAINED_FRAGMENT = "RetainedFragment";

    private static DecimalFormat df = new DecimalFormat("0.00");
    private PositionViewModel vm;
    private RetainFragment<Position> mRetainFragment;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_view);

        FragmentManager fm = getSupportFragmentManager();
        mRetainFragment = (RetainFragment<Position>) fm.findFragmentByTag(RETAINED_FRAGMENT);

        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment<>();
            fm.beginTransaction().add(mRetainFragment, RETAINED_FRAGMENT).commit();
        }

        vm = new PositionViewModel(this);
        vm.addObserver(this);

        String symbol = (String) getIntent().getSerializableExtra(EXTRA_POSITION_SYMBOL);
        Date date = (Date) getIntent().getSerializableExtra(EXTRA_POSITION_DATE);
        double count = (double) getIntent().getSerializableExtra(EXTRA_POSITION_COUNT);
        double price = (double) getIntent().getSerializableExtra(EXTRA_POSITION_PRICE);
        boolean div = (boolean) getIntent().getSerializableExtra(EXTRA_POSITION_DIV);

        if (mRetainFragment.data != null && mRetainFragment.data.getCurrPrice() != 0) {
            Log.d(TAG, "using retained Position object");
            vm.setPosition(mRetainFragment.data);
            updateViews();
        } else {
            vm.setPosition(new Position(symbol, count, price, date, div));
            vm.load();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mRetainFragment)
                    .commit();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // Log.d(TAG, "update() " + o.toString());
        updateViews();
    }

    private void updateViews() {
        Position position = vm.getPosition();
        mRetainFragment.data = position;

        // Core static fields
        setText(R.id.symbol,         position.getSymbol());
        setText(R.id.count,          vm.getCount() );
        setText(R.id.purchase_price, vm.getPurchasePrice() );
        setText(R.id.purchase_date,  vm.getPurchaseDate() );
        setText(R.id.purchase_value, vm.getPurchaseCost() );

        // Live data fields
        setText(R.id.dividends_earned, df.format(position.getDividendProfit()));
        setText(R.id.dividends_reinvested, position.IsDividendsReinvested() ? "Yes" : "No");

        // If dividends reinvested hide all the dividend related fields, its treated as part of the stock price in this case
        if(position.IsDividendsReinvested()) {
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

        setText(R.id.current_price, df.format(position.getCurrPrice()));
        setText(R.id.current_value, "$" + df.format(position.getCurrValue()));

        setText(R.id.profit, "$" + df.format(position.getProfit()));
        setText(R.id.price_percent_change, df.format(position.getPercentChanged()) + "%");

        if(position.getDividendProfit() != 0) {
            setText(R.id.total_percent_change, df.format(position.getPercentChangedWithDividends()) + "%");
            setText(R.id.profit_with_dividends, df.format(position.getProfit() + position.getDividendProfit()));
        }

        //Dividends
        if(position.IsDividendsReinvested()) {
            // TODO if dividends-Reinvested add new field for share count since that will have increased
            setText(R.id.current_count, Utils.getDecimalFormat3(position.getCurrCount()));
        }
        else {
            setText(R.id.last_dividend, df.format(position.getLastDividendPaid()));
            if (position.getLastDividendDate() != null) {
                setText(R.id.last_dividend_date, Utils.dateFormatLong.format(position.getLastDividendDate()));
                setText(R.id.next_dividend, Utils.dateFormatLong.format(position.getNextDividendEstimate()));
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
