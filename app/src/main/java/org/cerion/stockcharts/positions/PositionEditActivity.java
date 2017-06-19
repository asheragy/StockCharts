package org.cerion.stockcharts.positions;

import android.app.DialogFragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.Toast;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.DatePickerFragment;
import org.cerion.stockcharts.databinding.ActivityPositionEditBinding;
import org.cerion.stockcharts.ui.ViewModelActivity;
import org.cerion.stockcharts.viewmodel.PositionEditViewModel;

import java.util.Date;

public class PositionEditActivity extends ViewModelActivity<PositionEditViewModel>
        implements DatePickerFragment.OnDateSetListener,
        PositionEditViewModel.IView
{
    public static final String TAG = PositionEditActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPositionEditBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_position_edit);
        binding.setViewmodel(getViewModel());
    }

    @Override
    public void onSelectDate() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(Date date) {
        getViewModel().setDate(date);
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected PositionEditViewModel newViewModel() {
        return new PositionEditViewModel(this, this);
    }
}
