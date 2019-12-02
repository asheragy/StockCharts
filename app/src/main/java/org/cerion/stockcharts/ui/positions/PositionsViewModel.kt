package org.cerion.stockcharts.ui.positions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PositionsViewModel : ViewModel() {

    private val _accounts = MutableLiveData<List<String>>()
    val accounts: LiveData<List<String>>
        get() = _accounts

    //val testString = MutableLiveData<String>("Hello World")

    init {
        _accounts.value = listOf("Account 1", "Account 2")
    }
}
