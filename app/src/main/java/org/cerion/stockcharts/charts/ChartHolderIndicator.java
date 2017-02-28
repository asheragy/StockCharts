package org.cerion.stockcharts.charts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.functions.FunctionCall;
import org.cerion.stocklist.functions.FunctionDef;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.IPriceOverlay;
import org.cerion.stocklist.functions.Indicator;
import org.cerion.stocklist.model.Interval;

import java.util.Arrays;
import java.util.Comparator;

class ChartHolderIndicator extends ChartHolderBase {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();

    public ChartHolderIndicator(Context context, String symbol, Interval interval) {
        super(context, symbol, interval);

        mStockChart = new IndicatorChart(null);
        mCheckLogScale.setVisibility(View.GONE);

        final Indicator[] indicators = Indicator.values();
        Arrays.sort(indicators, new Comparator<Indicator>() {
            @Override
            public int compare(Indicator o1, Indicator o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.function);
        ArrayAdapter<Indicator> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, indicators);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Indicator f = indicators[position];
                Log.d(TAG, "onSelectFunction() " + f.toString());
                setIndicator(f);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setIndicator(indicators[0]);
        reload();
    }

    private IndicatorChart indicatorChart() {
        return (IndicatorChart)mStockChart;
    }

    private void setIndicator(Indicator indicator) {

        // Reset selection
        final FunctionDef def = indicator.getDef();
        final EditText[] fields = new EditText[def.paramCount()];
        //params().function = new FunctionCall(indicator, def.default_values);
        indicatorChart().setIndicator(new FunctionCall(indicator, def.default_values));

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE
                    //Get parameters and redraw chart
                    if (def.paramCount() > 0) {
                        Number p[] = getParameters(def.default_values);
                        indicatorChart().setIndicator( new FunctionCall( indicatorChart().getId(), p) );
                    }

                    mStockChart.clearOverlays();

                    // Get overlay parameters
                    for(int i = 0; i < mOverlays.getChildCount(); i++) {
                        OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);
                        indicatorChart().addOverlay((IOverlay)editControl.getOverlayFunction());
                    }

                    reload();
                    setInEditMode(false);
                } else {
                    setInEditMode(true);
                }
            }
        });

        // If overlay is not allowed then hide it
        if(def.result != FloatArray.class) {
            findViewById(R.id.add_overlay).setVisibility(View.GONE);
            mOverlays.removeAllViews();
        }
        else
            findViewById(R.id.add_overlay).setVisibility(View.VISIBLE);

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        layout.removeAllViews();
        for(int i = 0; i < def.paramCount(); i++) {
            Number n = def.default_values[i];
            fields[i] = getInputField(n);
            layout.addView(fields[i]);
        }
    }
}
