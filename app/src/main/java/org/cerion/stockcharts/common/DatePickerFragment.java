package org.cerion.stockcharts.common;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private OnDateSetListener mListener;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(mListener != null) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            mListener.onDateSet(c.getTime());
        }
    }

    public interface OnDateSetListener {
        void onDateSet(Date date);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Can implement either of these
        if (activity instanceof OnDateSetListener)
            mListener = (OnDateSetListener)activity;
        else if (!(activity instanceof DatePickerDialog.OnDateSetListener))
            throw new RuntimeException("DatePickerDialog.OnDateSetListener not implemented in activity");
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        if(mListener != null)
            return new DatePickerDialog(getActivity(), this, year, month, day);

        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day);
    }

}