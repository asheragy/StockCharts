package org.cerion.stockcharts.common

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

// Ref https://medium.com/fueled-engineering/binding-spinner-in-android-c5fa8c084480

@BindingAdapter(value = ["entries"])
fun Spinner.setEntries(entries: List<Any>) {
    val arrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, entries)
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    adapter = arrayAdapter
}

@BindingAdapter("selectedValue")
fun Spinner.setSelectedValue(selectedValue: Any?) {
    if (adapter != null ) {
        val position = (adapter as ArrayAdapter<Any>).getPosition(selectedValue)
        setSelection(position, false)
        tag = position
    }
}

@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun Spinner.getSelectedValue(): Any? {
    return selectedItem
}

@BindingAdapter("selectedValueAttrChanged")
fun Spinner.setInverseBindingListener(listener: InverseBindingListener?) {
    onItemSelectedListener = if (listener == null)
        null
    else {
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (tag != position) {
                    listener.onChange()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}