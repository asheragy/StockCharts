package org.cerion.stockcharts.common

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.LiveData
import org.cerion.stocks.core.model.Interval

@BindingAdapter(value = ["selectedInterval", "selectedIntervalAttrChanged"], requireAll = false)
fun bindInterval(spinner: Spinner, interval: LiveData<Interval>, listener: InverseBindingListener?) {
    if (interval.value != null) {
        spinner.setSelection(interval.value!!.ordinal)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                listener?.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}

@InverseBindingAdapter(attribute = "selectedInterval")
fun getSelectedInterval(spinner: Spinner): Interval {
    return Interval.values()[spinner.selectedItemPosition]
}


@BindingAdapter(value = ["selectedValue", "selectedValueAttrChanged"], requireAll = false)
fun bindSpinnerData(spinner: Spinner, newSelectedValue: Int, newTextAttrChanged: InverseBindingListener?) {


    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            newTextAttrChanged?.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    if (newSelectedValue != null) {
        //val pos = (spinner.adapter as ArrayAdapter<String?>).getPosition(newSelectedValue)
        spinner.setSelection(newSelectedValue, true)
    }

}

@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun captureSelectedValue(spinner: Spinner): Int {
    return spinner.selectedItemPosition
}
