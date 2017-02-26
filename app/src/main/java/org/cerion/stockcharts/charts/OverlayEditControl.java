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
import org.cerion.stocklist.functions.IFunction;
import org.cerion.stocklist.functions.IFunctionEnum;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.IPriceOverlay;
import org.cerion.stocklist.functions.Overlay;
import org.cerion.stocklist.functions.PriceOverlay;

import java.util.ArrayList;
import java.util.List;

public class OverlayEditControl extends ParametersEditControl {

    private static final String TAG = OverlayEditControl.class.getSimpleName();
    private OnDeleteListener onDeleteListener;
    private Spinner spOverlays;
    private List<IFunction> mOverlays = new ArrayList<>();

    public interface OnDeleteListener {
        void delete();
    }

    public OverlayEditControl(Context context, boolean prices) {
        super(context, R.layout.overlay_parameters); // TODO rename layout to match class name

        // Fill spinner
        spOverlays = (Spinner)findViewById(R.id.name);
        for(int i = 0; i < Overlay.values().length; i++) {
            mOverlays.add(Overlay.values()[i]);
        }

        if(prices) {
            for(int i = 0; i < PriceOverlay.values().length; i++) {
                mOverlays.add(PriceOverlay.values()[i]);
            }
        }

        ArrayAdapter<IFunction> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mOverlays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOverlays.setAdapter(adapter);
        spOverlays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IFunction o = mOverlays.get(position);
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

    @Deprecated
    public OverlayDataSet getDataSet() {
        int index = spOverlays.getSelectedItemPosition();
        IFunction overlay = mOverlays.get(index);
        FunctionDef overlayDef = overlay.getDef();
        Number p[] = getParameters(overlayDef.default_values);

        return new OverlayDataSet(overlay, p);
    }

    public IPriceOverlay getOverlayFunction() {
        int index = spOverlays.getSelectedItemPosition();
        IFunction overlay = mOverlays.get(index);
        FunctionDef overlayDef = overlay.getDef();
        Number p[] = getParameters(overlayDef.default_values);

        IPriceOverlay instance = getInstance(overlay);
        instance.setParams(p);
        return instance;
    }

    private IPriceOverlay getInstance(IFunction f) {
        if(f.getClass() == PriceOverlay.class) {
            return ((PriceOverlay)f).getInstance();
        }

        return ((Overlay)f).getInstance();
    }


}
