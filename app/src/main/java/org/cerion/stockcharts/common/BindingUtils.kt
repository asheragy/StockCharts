package org.cerion.stockcharts.common

import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

object BindingUtils {

    fun setTwoWayBinding(lifeCycleOwner: LifecycleOwner, checkBox: CheckBox, data: MutableLiveData<Boolean>) {
        data.observe(lifeCycleOwner) {
            checkBox.isChecked = it
        }

        checkBox.setOnClickListener {
            val checkbox = it as CheckBox
            data.value = checkbox.isChecked
        }
    }
}