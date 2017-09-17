package org.cerion.stockcharts.charts.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;

public class ParametersEditControl extends LinearLayout {

    private Number[] defaultParameters;

    public ParametersEditControl(Context context) {
        super(context);
    }

    public ParametersEditControl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setParameters(Number params[]) {
        defaultParameters = params;
        final EditText[] fields = new EditText[params.length];

        removeAllViews();
        for(int i = 0; i < params.length; i++) {
            Number n = params[i];
            fields[i] = getInputField(n);
            addView(fields[i]);
        }
    }

    public Number[] getParameters() {
        LinearLayout parameters = (LinearLayout)findViewById(R.id.parameters);
        Number p[] = defaultParameters.clone();

        if(p.length != parameters.getChildCount())
            throw new IllegalStateException("expected parameters do not match layout count");

        for (int i = 0; i < p.length; i++) {
            EditText field = (EditText)parameters.getChildAt(i);
            try {
                String entered = field.getText().toString();
                if (p[i] instanceof Integer) {
                    p[i] = Integer.parseInt(entered);
                } else {
                    p[i] = Float.parseFloat(entered);
                }
            } catch(Exception e) {
                // Reset input field to default value
                field.setText(p[i].toString());
            }
        }

        return p;
    }

    private EditText getInputField(Number n) {
        final EditText input = new EditText(getContext());
        input.setText(n.toString());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        return input;
    }
}
