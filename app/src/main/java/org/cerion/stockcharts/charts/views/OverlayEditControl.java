package org.cerion.stockcharts.charts.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.functions.IFunctionEnum;
import org.cerion.stocklist.functions.IOverlay;
import org.cerion.stocklist.functions.Overlay;

import java.util.List;

public class OverlayEditControl extends LinearLayout {

    private static final String TAG = OverlayEditControl.class.getSimpleName();
    private OnDeleteListener onDeleteListener;
    private Spinner spOverlays;
    private List<FunctionAdapterItem> mOverlays;
    private EditText[] mFields;
    private ParametersEditControl parameters;

    public interface OnDeleteListener {
        void delete();
    }

    public OverlayEditControl(Context context, IFunctionEnum[] overlays) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_overlay_edit_control, this, true);

        parameters = (ParametersEditControl)findViewById(R.id.parameters);
        mOverlays = FunctionAdapterItem.getList(overlays);
        //Collections.sort(overlays);

        ArrayAdapter<FunctionAdapterItem> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mOverlays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spOverlays = (Spinner)findViewById(R.id.name);
        spOverlays.setAdapter(adapter);
        spOverlays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IOverlay o = (IOverlay)mOverlays.get(position).function;
                Log.d(TAG, "onSelectOverlay() " + o.toString());
                //EditText[] fields = new EditText[o.params().length];

                // Add parameters
                parameters.setParameters(o.params());
                /*
                LinearLayout layout = (LinearLayout)findViewById(R.id.parameters);
                layout.removeAllViews();
                for(int i = 0; i < o.params().length; i++) {
                    Number n = o.params()[i];
                    fields[i] = getInputField(n);
                    layout.addView(fields[i]);
                }
                */
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

        spOverlays.setSelection(FunctionAdapterItem.indexOf(mOverlays, Overlay.EMA));
    }

    public void setOnDelete(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public IOverlay getOverlayFunction() {
        int index = spOverlays.getSelectedItemPosition();
        IOverlay function = (IOverlay)mOverlays.get(index).function;

        Number[] params = parameters.getParameters();
        function.setParams(params);

        return function;
    }

    public void setOverlayFunction(IOverlay overlay) {

        for(int i = 0; i < mOverlays.size(); i++) {
            FunctionAdapterItem item = mOverlays.get(i);
            if (item.function.getId() == overlay.getId()) {
                item.function = overlay;
                spOverlays.setSelection(i);
                break;
            }
        }
    }
}
