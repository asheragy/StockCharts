package org.cerion.stockcharts.ui.crypto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cerion.marketdata.webclients.coingecko.CoinGecko


data class CryptoRow(val name: String,
                     val symbol: String) {
    var price: Double = 0.0
    var change24h: Double = 0.0
}

class CryptoViewModel : ViewModel() {

    private val api = CoinGecko()

    private val _rows = MutableLiveData<List<CryptoRow>>()
    val rows: LiveData<List<CryptoRow>>
        get() = _rows

    private val mappings = mapOf(
        "bitcoin" to CryptoRow("Bitcoin","BTC-USD"),
        "matic-network" to CryptoRow("Polygon/Matic", "MATIC-USD"),
        "algorand" to CryptoRow("Algorand","ALGO-USD"),
        "ethereum" to CryptoRow("Ethereum","ETH-USD"),
        "solana" to CryptoRow("Solana","SOL-USD")
    )

    fun load() {
        viewModelScope.launch {

            val result = withContext(Dispatchers.IO) {
                val ids = mappings.keys.toList()
                val response = api.getPrices(ids)

                response.forEach {
                    mappings[it.id]?.apply {
                        this.price = it.price
                        this.change24h = it.change24h
                    }
                }

                mappings.values.toList()
            }

            _rows.value = result.sortedBy { it.name }
        }
    }

}