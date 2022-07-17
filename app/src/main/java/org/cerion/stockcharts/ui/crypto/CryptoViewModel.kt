package org.cerion.stockcharts.ui.crypto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


data class CryptoRow(val id: String, val price: Double, val symbol: String)

class CryptoViewModel : ViewModel() {

    private val _rows = MutableLiveData<List<CryptoRow>>()
    val rows: LiveData<List<CryptoRow>>
        get() = _rows


    fun load() {
        _rows.value = listOf(
            CryptoRow("Bitcoin", 1.0, "BTC-USD"),
            CryptoRow("Polygon/Matic", 1.0, "MATIC-USD"),
            CryptoRow("Algorand", 1.0, "ALGO-USD"),
            CryptoRow("Ethereum", 123.45, "ETH-USD"),
            CryptoRow("Solana", 67.89, "SOL-USD")).sortedBy { it.id }
    }

}