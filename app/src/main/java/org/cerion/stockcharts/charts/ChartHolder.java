package org.cerion.stockcharts.charts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.Function;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.data.FloatArray;
import org.cerion.stocklist.model.FunctionCall;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.FunctionId;

class ChartHolder extends LinearLayout { //TODO I think this can just extend View

    private final Context mContext;
    ChartParams mChartParams = new ChartParams();
    PriceList mList;

    public ChartHolder(final Context context, PriceList list, FunctionId id) {
        super(context);

        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chart_holder, this, true);

        final FunctionDef def = (id != null ? Function.getDef(id) : null);
        final EditText[] fields = (def != null ? new EditText[def.param_count] : null);
        mList = list;

        if(id != null && def != null) {
            mChartParams.function = new FunctionCall(id, def.default_values);
        }

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);
                Button button = (Button)v;

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

                        FrameLayout frame = (FrameLayout) findViewById(R.id.chart_frame);

                        Chart chart = ChartHelper.getLineChart(context, mList, mChartParams);
                        frame.removeAllViews();
                        frame.addView(chart);
                    }

                    button.setText("Edit");
                    controls.setVisibility(View.GONE);
                } else { // EDIT
                    button.setText("Save");
                    controls.setVisibility(View.VISIBLE);
                }
            }
        });

        // If overlay is not allowed then hide it
        if(def != null && def.result != FloatArray.class) {
            findViewById(R.id.add_overlay).setVisibility(View.GONE);
        }

        // Add parameters
        LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
        if(def != null) {
            for(int i = 0; i < def.param_count; i++) {
                Number n = def.default_values[i];
                fields[i] = getInputField(n);
                layout.addView(fields[i]);
            }
        }
    }

    public void addOverlay(Overlay overlay) {
        mChartParams.overlays.add(overlay);
        FrameLayout frame = (FrameLayout)findViewById(R.id.chart_frame);
        Chart chart = ChartHelper.getLineChart(mContext, mList, mChartParams);
        frame.removeAllViews();
        frame.addView(chart);
    }

    public EditText getInputField(Number n) {
        final EditText input = new EditText(mContext);
        input.setText(n.toString());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        return input;
    }


}
