package org.cerion.stockcharts.common;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.cerion.stockcharts.R;

public class MasterFloatingActionButton extends FrameLayout {

    private TextView text;

    public MasterFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MasterFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_fab_master, this, true);
        text = (TextView)findViewById(R.id.text);
    }

    public void open() {
        text.setText("X");
    }

    public void close() {
        text.setText("+");
    }
}
