package org.cerion.stockcharts.ui.crypto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


data class CryptoRow(val id: String, val price: Double)

class CryptoViewModel : ViewModel() {

    private val _rows = MutableLiveData<List<CryptoRow>>()
    val rows: LiveData<List<CryptoRow>>
        get() = _rows


    fun load() {
        _rows.value = listOf(CryptoRow("Ethereum", 123.45), CryptoRow("Solana", 67.89))
    }

}