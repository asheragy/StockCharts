package org.cerion.stockcharts.charts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.functions.FunctionCall;
import org.cerion.stocklist.functions.FunctionDef;
import org.cerion.stocklist.functions.Indicator;
import org.cerion.stocklist.model.Interval;

class ChartHolderIndicator extends ChartHolderBase {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();
    private CheckBox mCheckLogScale;

    public ChartHolderIndicator(Context context, String symbol, Interval interval) {
        super(context, symbol, interval);

        mCheckLogScale = (CheckBox)findViewById(R.id.check_logscale);

        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.function);
        final String[] functions = new String[Indicator.values().length];
        for(int i = 0; i < functions.length; i++) {
            functions[i] = Indicator.values()[i].toString();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, functions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Indicator f = Indicator.values()[position];
                Log.d(TAG, "onSelectFunction() " + f.toString());
                setIndicator(f);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        findViewById(R.id.add_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddOverlay();
            }
        });

        setIndicator(Indicator.values()[0]);
        reload();
    }

    private void setIndicator(Indicator indicator) {

        // Reset selection
        final FunctionDef def = indicator.getDef();
        final EditText[] fields = new EditText[def.paramCount()];
        mChartParams.function = new FunctionCall(indicator, def.default_values);

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE
                    //Get parameters and redraw chart
                    if (def.paramCount() > 0) {
                        Number p[] = getParameters(def.default_values);
                        mChartParams.function = new FunctionCall(mChartParams.function.id, p);
                    }

                    OverlayDataSet.resetColors();
                    mChartParams.overlays.clear();

                    // Get overlay parameters
                    for(int i = 0; i < mOverlays.getChildCount(); i++) {
                        OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);
                        mChartParams.overlays.add(editControl.getDataSet());
                    }

                    mChartParams.logscale = mCheckLogScale.isChecked();
                    //mListener.onRequest(ChartHolder.this, mSymbol);
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
        }

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        layout.removeAllViews();
        for(int i = 0; i < def.paramCount(); i++) {
            Number n = def.default_values[i];
            fields[i] = getInputField(n);
            layout.addView(fields[i]);
        }
    }

    @Override
    public Chart getChart() {
        if(mChartParams == null || mChartParams.function == null)
            return mChartFactory.getEmptyChart();

        return mChartFactory.getIndicatorChart(mChartParams);
    }
    
}
