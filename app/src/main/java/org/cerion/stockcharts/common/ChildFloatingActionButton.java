package org.cerion.stockcharts.common;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cerion.stockcharts.R;

public class ChildFloatingActionButton extends LinearLayout {

    public ChildFloatingActionButton(Context context, String text) {
        super(context, null);
        init(text);
    }

    public ChildFloatingActionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        String text = "";
        if (attrs != null)
            text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");

        init(text);
    }

    private void init(String text) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_fab_child, this);

        if (isInEditMode())
            text = "Test";

        TextView label = (TextView)findViewById(R.id.text_label);
        TextView button = (TextView)findViewById(R.id.text_button);

        label.setText(text);
        button.setText(text.substring(0,2));
    }


}
