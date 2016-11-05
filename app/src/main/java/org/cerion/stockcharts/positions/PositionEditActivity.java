package org.cerion.stockcharts.positions;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.DatePickerFragment;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.model.Position;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class PositionEditActivity extends AppCompatActivity
        implements DatePickerFragment.OnDateSetListener {

    public static final String TAG = PositionEditActivity.class.getSimpleName();
    private EditText mSymbol;
    private EditText mCount;
    private EditText mPrice;
    private Date mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_edit);

        mSymbol = (EditText)findViewById(R.id.symbol);
        mCount = (EditText)findViewById(R.id.count);
        mPrice = (EditText)findViewById(R.id.price);

        findViewById(R.id.date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");

            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String symbol = mSymbol.getText().toString();
                    float count = Float.parseFloat(mCount.getText().toString());
                    float price = Float.parseFloat(mPrice.getText().toString());
                    boolean dr = ((CheckBox)findViewById(R.id.dividends_reinvested)).isChecked();

                    Position p = new Position(symbol, count, price, mDate, dr);

                    Log.d(TAG, "Saving " + p + " " + Utils.dateFormatShort.format(p.getDate()));

                    StockDB db = StockDB.getInstance(PositionEditActivity.this);
                    db.addPosition(p);
                    finish();
                }
                catch(Exception e) {
                    Log.d(TAG, "Failed to parse value");
                }

            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onDateSet(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        mDate = date;
        ((Button)findViewById(R.id.date)).setText( sdf.format(date) );
    }
}
