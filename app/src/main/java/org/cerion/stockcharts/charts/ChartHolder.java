package org.cerion.stockcharts.charts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.arrays.VolumeArray;
import org.cerion.stocklist.indicators.FunctionCall;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.Function;
import org.cerion.stocklist.model.Interval;

class ChartHolder extends ParametersEditControl {

    private static final String TAG = ChartHolder.class.getSimpleName();

    ChartParams mChartParams = new ChartParams();
    PriceList mList;
    private String mSymbol;
    private OnDataRequestListener mListener;
    private ChartFactory mChartFactory;
    private LinearLayout mOverlays;

    public interface OnDataRequestListener {
        void onRequest(ChartHolder holder, String symbol, Interval interval);
    }

    public ChartHolder(Context context, String symbol) {
        super(context, R.layout.chart_holder);

        mChartFactory = new ChartFactory(context);
        mSymbol = symbol;
        mListener = (OnDataRequestListener)getContext();

        mOverlays = (LinearLayout)findViewById(R.id.overlays);
        mOverlays.removeAllViews(); // remove placeholder used in design viewer

        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.function);
        final String[] functions = new String[Function.values().length];
        for(int i = 0; i < functions.length; i++) {
            functions[i] = Function.values()[i].toString();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, functions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Function f = Function.values()[position];
                Log.d(TAG, "onSelectFunction() " + f.toString());
                onSelect(f);
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

        reload();
    }

    private void onAddOverlay() {
        final OverlayEditControl control = new OverlayEditControl(getContext());
        control.setOnDelete(new OverlayEditControl.OnDeleteListener() {
            @Override
            public void delete() {
                mOverlays.removeView(control);
            }
        });

        mOverlays.addView(control);
    }

    private void onSelect(Function function) {

        // Reset selection
        mChartParams = new ChartParams();
        final FunctionDef def = function.getDef();
        final EditText[] fields = new EditText[def.param_count];
        mChartParams.function = new FunctionCall(function, def.default_values);

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE
                    //Get parameters and redraw chart
                    if (def.param_count > 0) {
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

                    mListener.onRequest(ChartHolder.this, mSymbol, getInterval());
                    setInEditMode(false);
                } else {
                    setInEditMode(true);
                }
            }
        });

        // If overlay is not allowed then hide it
        if(def.result != FloatArray.class && def.result != VolumeArray.class) {
            findViewById(R.id.add_overlay).setVisibility(View.GONE);
        }

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        layout.removeAllViews();
        for(int i = 0; i < def.param_count; i++) {
            Number n = def.default_values[i];
            fields[i] = getInputField(n);
            layout.addView(fields[i]);
        }
    }

    private void setInEditMode(boolean bEdit) {
        Button button = (Button)findViewById(R.id.save_edit_button);
        View controls = findViewById(R.id.edit_layout);

        if(bEdit) {
            button.setText("Save");
            //controls.setScaleY(0);
            //controls.setVisibility(View.VISIBLE);
            controls.setVisibility(View.VISIBLE);
            //controls.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.view_show));
            //controls.setX(controls.getHeight());
            //controls.animate().scaleY(1).setDuration(500);
        } else {
            button.setText("Edit");
            //controls.setScaleY(1);
            //controls.setY();
            //controls.animate().translationY(0).setDuration(500);
            controls.setVisibility(View.GONE);
            //controls.animate()
            //        .translationY(0)
            //        .alpha(0);
        }

    }

    private Interval getInterval() {
        Spinner spInterval = (Spinner)findViewById(R.id.interval);
        Interval interval = Interval.DAILY;
        if(spInterval.getSelectedItemPosition() == 1)
            interval = Interval.WEEKLY;
        if(spInterval.getSelectedItemPosition() == 2)
            interval = Interval.MONTHLY;

        return interval;
    }

    public void loadChart(PriceList list) {
        mList = list;
        reload();
    }

    public void addOverlay(OverlayDataSet overlay) {
        mChartParams.overlays.add(overlay);
        reload();
    }

    private void reload() {
        FrameLayout frame = (FrameLayout)findViewById(R.id.chart_frame);
        frame.removeAllViews();

        Chart chart;
        if(mList != null)
            chart = mChartFactory.getChart(mList, mChartParams);
        else
            chart = mChartFactory.getEmptyChart();

        frame.addView(chart);
    }

}
