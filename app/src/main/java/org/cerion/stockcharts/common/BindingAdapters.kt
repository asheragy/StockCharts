package org.cerion.stockcharts.common

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener


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
