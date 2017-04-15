package org.cerion.stockcharts.charts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.charts.IndicatorChart;
import org.cerion.stocklist.functions.IFunction;
import org.cerion.stocklist.functions.IFunctionEnum;
import org.cerion.stocklist.functions.IIndicator;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.ISimpleOverlay;
import org.cerion.stocklist.functions.Indicator;
import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class ChartHolderIndicator extends ChartHolderBase {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();

    public ChartHolderIndicator(Context context, String symbol, IndicatorChart chart) {
        super(context, symbol, chart);

        mCheckLogScale.setVisibility(View.GONE);

        final List<FunctionAdapterItem> items = FunctionAdapterItem.getList(Indicator.values());

        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.function);
        ArrayAdapter<FunctionAdapterItem> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        IFunctionEnum eval = Indicator.MACD;
        if (chart.getId() != null) {
            eval = chart.getId();
        }

        int index = FunctionAdapterItem.indexOf(items, eval);
        // Log.d(TAG, "setting selection " + index + " " + sp.getSelectedItemPosition());
        sp.setSelection(index);
        setIndicator((IIndicator)items.get(index).function);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IIndicator f = (IIndicator)items.get(position).function;
                Log.d(TAG, "onSelectFunction() " + f.toString());
                setIndicator(f);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private IndicatorChart indicatorChart() {
        return (IndicatorChart)mStockChart;
    }

    private void setIndicator(IIndicator ii) {
        Indicator indicator = ii.getId();

        // Reset selection
        final IIndicator instance = indicator.getInstance();
        final Number[] params = instance.params();
        final EditText[] fields = new EditText[params.length];

        indicatorChart().setIndicator(instance);

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE
                    //Get parameters and redraw chart
                    if (params.length > 0) {
                        Number p[] = getParameters(params);
                        instance.setParams(p);
                        indicatorChart().setIndicator( instance );
                    }

                    mStockChart.clearOverlays();

                    // Get overlay parameters
                    for(int i = 0; i < mOverlays.getChildCount(); i++) {
                        OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);
                        indicatorChart().addOverlay((ISimpleOverlay)editControl.getOverlayFunction());
                    }

                    reload();
                    setInEditMode(false);
                } else {
                    setInEditMode(true);
                }
            }
        });

        // If overlay is not allowed then hide it
        if(instance.getResultType() != FloatArray.class) {
            findViewById(R.id.add_overlay).setVisibility(View.GONE);
            mOverlays.removeAllViews();
        }
        else
            findViewById(R.id.add_overlay).setVisibility(View.VISIBLE);

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        layout.removeAllViews();
        for(int i = 0; i < params.length; i++) {
            Number n = params[i];
            fields[i] = getInputField(n);
            layout.addView(fields[i]);
        }

        reload();
    }
}
