package org.cerion.stockcharts.charts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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

class ChartHolder extends LinearLayout {

    ChartParams mChartParams = new ChartParams();
    PriceList mList;
    private String mSymbol;
    private OnDataRequestListener mListener;
    private ChartFactory mChartFactory;

    public interface OnDataRequestListener {
        void onRequest(ChartHolder holder, String symbol, Interval interval);
    }

    public ChartHolder(Context context, String symbol, Function id) {
        super(context);

        mChartFactory = new ChartFactory(context);
        mSymbol = symbol;
        mListener = (OnDataRequestListener)getContext();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_holder, this, true);

        final FunctionDef def = id.getDef();
        final EditText[] fields = new EditText[def.param_count];
        mChartParams.function = new FunctionCall(id, def.default_values);

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE
                    //Get parameters and redraw chart
                    if (def != null && def.param_count > 0) {

                        Number p[] = def.default_values; // Use defaults in-case anything is invalid
                        for (int i = 0; i < def.param_count; i++) {
                            String entered = fields[i].getText().toString();
                            if (p[i] instanceof Integer) {
                                p[i] = Integer.parseInt(entered);
                            } else {
                                p[i] = Double.parseDouble(entered);
                            }
                        }
                        mChartParams.function = new FunctionCall(mChartParams.function.id, p);
                    }

                    mListener.onRequest(ChartHolder.this, mSymbol, getInterval());

                    changeMode(false);
                } else { // EDIT
                    changeMode(true);
                }
            }
        });

        // If overlay is not allowed then hide it
        if(def.result != FloatArray.class && def.result != VolumeArray.class) {
            findViewById(R.id.add_overlay).setVisibility(View.GONE);
        }

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        for(int i = 0; i < def.param_count; i++) {
            Number n = def.default_values[i];
            fields[i] = getInputField(n);
            layout.addView(fields[i]);
        }

        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.function);
        String[] functions = new String[Function.values().length];
        for(int i = 0; i < functions.length; i++) {
            functions[i] = Function.values()[i].toString();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, functions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        reload();
    }

    public void changeMode(boolean bEdit) {
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

    public Interval getInterval() {
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

    public EditText getInputField(Number n) {
        final EditText input = new EditText(getContext());
        input.setText(n.toString());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        return input;
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
