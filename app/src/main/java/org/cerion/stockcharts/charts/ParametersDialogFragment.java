package org.cerion.stockcharts.charts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.cerion.stockcharts.MainActivity;
import org.cerion.stocklist.Function;
import org.cerion.stocklist.model.FunctionCall;
import org.cerion.stocklist.model.FunctionDef;
import org.cerion.stocklist.model.FunctionId;

public class ParametersDialogFragment extends DialogFragment {

    private static final String EXTRA_ID = "id";

    public static ParametersDialogFragment newInstance(FunctionId functionId) {
        ParametersDialogFragment frag = new ParametersDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_ID, functionId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FunctionId id = (FunctionId)getArguments().getSerializable(EXTRA_ID);
        final FunctionDef def = Function.getDef(id);
        final EditText[] params = new EditText[def.param_count];


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                //.setIcon(R.drawable.alert_dialog_icon)
                .setTitle("Parameters");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        for(int i = 0; i < def.param_count; i++) {
            Number n = def.default_values[i];
            params[i] = getInput(n);
            layout.addView(params[i]);
        }

        builder.setView(layout);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Number p[] = def.default_values;
                        for(int i = 0; i < def.param_count; i++)
                        {
                            String entered = params[i].getText().toString();
                            if(p[i] instanceof Integer)
                            {
                                p[i] = Integer.parseInt(entered);
                            }
                            else
                            {
                                p[i] = Double.parseDouble(entered);
                            }
                        }

                        FunctionCall call = new FunctionCall(id,p);
                        //((FragmentAlertDialog)getActivity()).doPositiveClick();
                    }
                });

        builder.setNegativeButton("Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //((FragmentAlertDialog)getActivity()).doNegativeClick();
                }
            });



        return builder.create();
    }

    public EditText getInput(Number n)
    {
        final EditText input = new EditText(getActivity());
        input.setText(n.toString());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        return input;
    }
}
