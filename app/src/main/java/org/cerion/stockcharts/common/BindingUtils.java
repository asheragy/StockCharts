package org.cerion.stockcharts.common;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import android.util.Log;
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
