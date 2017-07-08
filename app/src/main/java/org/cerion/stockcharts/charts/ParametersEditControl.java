package org.cerion.stockcharts.charts;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;

public abstract class ParametersEditControl extends LinearLayout {

    public ParametersEditControl(Context context) {
        super(context);
    }

    public ParametersEditControl(Context context, int layout) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layout, this, true);
    }

    protected Number[] getParameters(Number[] defaultValues) {
        LinearLayout parameters = (LinearLayout)findViewById(R.id.parameters);
        Number p[] = defaultValues.clone();

        if(defaultValues.length != parameters.getChildCount())
            throw new IllegalStateException("expected parameters do not match layout count");

        for (int i = 0; i < defaultValues.length; i++) {
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
                field.setText(p[i] + "");
            }
        }

        return p;
    }

    protected EditText getInputField(Number n) {
        final EditText input = new EditText(getContext());
        input.setText(n.toString());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        return input;
    }
}
