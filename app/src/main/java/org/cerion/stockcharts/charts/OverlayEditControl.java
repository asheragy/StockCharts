package org.cerion.stockcharts.charts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.Overlay;
import org.cerion.stocklist.overlays.ExpMovingAverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverlayEditControl extends ParametersEditControl {

    private static final String TAG = OverlayEditControl.class.getSimpleName();
    private OnDeleteListener onDeleteListener;
    private Spinner spOverlays;
    private List<OverlayHolder> mOverlays;

    public interface OnDeleteListener {
        void delete();
    }

    public OverlayEditControl(Context context, List<IOverlay> overlays) {
        super(context, R.layout.overlay_parameters); // TODO rename layout to match class name

        mOverlays = OverlayHolder.getList(overlays);
        Collections.sort(mOverlays);
        int index = 0;
        for(int i = 0; i < mOverlays.size(); i++) {
            if(mOverlays.get(i).overlay instanceof ExpMovingAverage)
                index = i;
        }

        ArrayAdapter<OverlayHolder> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mOverlays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spOverlays = (Spinner)findViewById(R.id.name);
        spOverlays.setAdapter(adapter);
        spOverlays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IOverlay o = mOverlays.get(position).overlay;
                Log.d(TAG, "onSelectOverlay() " + o.toString());
                EditText[] fields = new EditText[o.params().length];

                // Add parameters
                LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
                layout.removeAllViews();
                for(int i = 0; i < o.params().length; i++) {
                    Number n = o.params()[i];
                    fields[i] = getInputField(n);
                    layout.addView(fields[i]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Remove button
        findViewById(R.id.remove).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDeleteListener != null)
                    onDeleteListener.delete();
            }
        });

        spOverlays.setSelection(index);
    }

    public void setOnDelete(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public IOverlay getOverlayFunction() {
        int index = spOverlays.getSelectedItemPosition();
        return mOverlays.get(index).overlay;
    }

    private static class OverlayHolder implements Comparable<OverlayHolder>
    {
        IOverlay overlay;
        OverlayHolder(IOverlay o) {
            overlay = o;
        }

        static List<OverlayHolder> getList(List<IOverlay> overlays) {
            List<OverlayHolder> result = new ArrayList<>();
            for(IOverlay o : overlays)
                result.add(new OverlayHolder(o));

            return result;
        }

        @Override
        public String toString() {
            return overlay.getName();
        }

        @Override
        public int compareTo(OverlayHolder o) {
            return this.toString().compareTo(o.toString());
        }
    }
}
