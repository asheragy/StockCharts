package org.cerion.stockcharts.common;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class BindingUtils {
    private static final String TAG = BindingUtils.class.getSimpleName();

    @BindingAdapter({"selection"})
    public static void setSelection(final Spinner spinner, final int position) {
        Log.d(TAG, "setSelection");
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(position);
            }
        });
    }

    /*


    @InverseBindingAdapter(attribute = "selection")
    public static int getSelection(Spinner spinner) {
        Log.d(TAG, "bindSelectionInverseAdapter");
        return spinner.getSelectedItemPosition();
    }

    @BindingAdapter(value = "selectionAttrChanged")
    public static void bindSelectionChanged(Spinner spinnner, final InverseBindingListener newAttrChanged) {
        Log.d(TAG, "bindSelectionChanged");

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newAttrChanged.onChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newAttrChanged.onChange();
            }
        };

        spinnner.setOnItemSelectedListener(listener);
    }
    */
}
