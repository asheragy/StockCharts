package org.cerion.stockcharts.charts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.functions.FunctionDef;
import org.cerion.stocklist.functions.Overlay;

public class OverlayEditControl extends ParametersEditControl {

    private static final String TAG = OverlayEditControl.class.getSimpleName();
    private OnDeleteListener onDeleteListener;

    public interface OnDeleteListener {
        void delete();
    }

    public OverlayEditControl(Context context) {
        super(context, R.layout.overlay_parameters); // TODO rename layout to match class name

        // Fill spinner
        Spinner sp = (Spinner)findViewById(R.id.name);
        final String[] overlays = new String[Overlay.values().length];
        for(int i = 0; i < overlays.length; i++) {
            overlays[i] = Overlay.values()[i].toString();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, overlays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Overlay o = Overlay.values()[position];
                Log.d(TAG, "onSelectOverlay() " + o.toString());

                FunctionDef def = o.getDef();
                EditText[] fields = new EditText[def.paramCount()];

                // Add parameters
                LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
                layout.removeAllViews();
                for(int i = 0; i < def.paramCount(); i++) {
                    Number n = def.default_values[i];
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
    }

    public void setOnDelete(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public OverlayDataSet getDataSet() {
        Spinner name = (Spinner)findViewById(R.id.name);
        int index = name.getSelectedItemPosition();
        Overlay overlay = Overlay.values()[index];
        FunctionDef overlayDef = overlay.getDef();

        Number p[] = getParameters(overlayDef.default_values);

        return new OverlayDataSet(overlay, p);
    }
}
