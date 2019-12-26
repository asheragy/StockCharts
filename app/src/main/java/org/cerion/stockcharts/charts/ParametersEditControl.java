package org.cerion.stockcharts.charts;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.cerion.stockcharts.R;

import java.util.List;

public class ParametersEditControl extends LinearLayout {

    private Number[] defaultParameters;

    public ParametersEditControl(Context context) {
        super(context);
    }

    public ParametersEditControl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setParameters(final List<Number> params) {

        // TODO add binding so EditText updates corresponding array at all times

        defaultParameters = params.toArray(new Number[0]);
        //final EditText[] fields = new EditText[params.length];

        removeAllViews();
        for(int i = 0; i < params.size(); i++) {
            Number n = params.get(i);
            final int pos = i;
            EditText et = getInputField(n);

            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TryParse
                    Number defValue = defaultParameters[pos];
                    Number newValue = tryParseNumber(s.toString(), defValue);

                    params.set(pos, newValue);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            addView(et);
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
                // TODO may be unnecessary, we are only getting values
                // Reset input field to default value
                field.setText(p[i].toString());
            }
        }

        return p;
    }

    private Number tryParseNumber(String text, Number defaultVal) {
        try {
            if (defaultVal instanceof Integer) {
                return Integer.parseInt(text);
            } else {
                return Float.parseFloat(text);
            }
        } catch(Exception e) {
            return defaultVal;
        }
    }

    private EditText getInputField(Number n) {
        final EditText input = new EditText(getContext());
        input.setText(n.toString());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        return input;
    }
}
