package org.cerion.stockcharts.charts;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stocklist.arrays.FloatArray;
import org.cerion.stocklist.indicators.FunctionCall;
import org.cerion.stocklist.model.Function;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.Interval;

class ChartHolder extends ParametersEditControl {

    private static final String TAG = ChartHolder.class.getSimpleName();

    ChartParams mChartParams;
    //PriceList mList;
    private String mSymbol;
    private ChartFactory mChartFactory;
    private LinearLayout mOverlays;
    private CheckBox mCheckLogScale;

    public ChartHolder(Context context, String symbol, Interval interval) {
        super(context, R.layout.view_chart_holder);

        mChartFactory = new ChartFactory(context);
        mSymbol = symbol;

        mCheckLogScale = (CheckBox)findViewById(R.id.check_logscale);
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

        mChartParams = new ChartParams(mSymbol);
        mChartParams.interval = interval;
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

    /*
    private Interval getInterval() {
        Spinner spInterval = (Spinner)findViewById(R.id.interval);
        Interval interval = Interval.DAILY;
        if(spInterval.getSelectedItemPosition() == 1)
            interval = Interval.WEEKLY;
        if(spInterval.getSelectedItemPosition() == 2)
            interval = Interval.MONTHLY;

        return interval;
    }
    */

    /*
    public void loadChart(PriceList list) {
        mList = list;
        reload();
    }
    */

    /*
    public void addOverlay(OverlayDataSet overlay) {
        mChartParams.overlays.add(overlay);
        reload();
    }
    */

    private void setChart(Chart chart) {
        final FrameLayout frame = (FrameLayout) findViewById(R.id.chart_frame);
        frame.removeAllViews();
        frame.addView(chart);
    }

    public void reload(Interval interval) {
        mChartParams.interval = interval;
        reload();
    }

    private void reload() {
        if(mChartParams != null && mChartParams.function != null)
            reload_async();
        else
            setChart(mChartFactory.getEmptyChart());
    }

    private void reload_async() {
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.loading_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {

            Chart chart;
            @Override
            public void run() {
                if (Looper.myLooper() == null)
                    Looper.prepare(); // This is needed to create a new chart instance inside a different thread

                chart = mChartFactory.getChart(mChartParams);
                if( Looper.myLooper() != null)
                    Looper.myLooper().quit();
            }

            @Override
            public void onFinish() {
                setChart(chart);
                progressBar.setVisibility(View.GONE);
            }
        });

        task.execute();
    }

}
