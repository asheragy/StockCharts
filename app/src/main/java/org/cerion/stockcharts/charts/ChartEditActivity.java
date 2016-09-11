package org.cerion.stockcharts.charts;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.cerion.stockcharts.R;

public class ChartEditActivity extends AppCompatActivity {

    private Spinner mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_edit_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String arr[] = { "Price", "RSI", "MACD" };
        mIndicator = (Spinner)findViewById(R.id.indicator_spinner);


        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIndicator.setAdapter(adapter);

    }

    //TODO, class for indicator and required parameters
    //Add adapter for it

}
