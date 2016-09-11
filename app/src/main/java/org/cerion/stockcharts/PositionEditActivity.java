package org.cerion.stockcharts;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.model.Position;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PositionEditActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

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
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String formattedDate = sdf.format(c.getTime());

        mDate = c.getTime();
        ((Button)findViewById(R.id.date)).setText(formattedDate);
    }

    // TODO add to seperate file and include listener parameter for result
    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day);
        }
    }
}
