package org.cerion.stockcharts.ui.crypto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cerion.marketdata.core.model.Position
import org.cerion.marketdata.webclients.coingecko.CoinGecko


data class CryptoRow(val name: String,
                     val symbol: String) {
    var quote: CoinGecko.DetailedQuote? = null
}

data class CryptoPosition(val row: CryptoRow, override val quantity: Double) : Position {
    override val symbol = row.symbol.substringBefore("-")
    override val pricePerShare = row.quote?.price ?: 0.0
    override val totalValue = quantity * pricePerShare
    override val cash = false
}

class CryptoViewModel : ViewModel() {

    private val api = CoinGecko()

    private val _rows = MutableLiveData<List<CryptoRow>>()
    val rows: LiveData<List<CryptoRow>>
        get() = _rows

    private val _positions = MutableLiveData<List<CryptoPosition>>()
    val positions: LiveData<List<CryptoPosition>>
        get() = _positions

    private val mappings = mapOf(
        "bitcoin" to CryptoRow("Bitcoin","BTC-USD"),
        "matic-network" to CryptoRow("Polygon/Matic", "MATIC-USD"),
        "algorand" to CryptoRow("Algorand","ALGO-USD"),
        "ethereum" to CryptoRow("Ethereum","ETH-USD"),
        "solana" to CryptoRow("Solana","SOL-USD"),
        "binancecoin" to CryptoRow("BNB","BNB-USD"),
        "litecoin" to CryptoRow("Litecoin","LTC-USD")
    )

    fun load() {
        viewModelScope.launch {

            val result = withContext(Dispatchers.IO) {
                val ids = mappings.keys.toList()
                val response = api.getDetailedQuotes(ids)

                response.forEach {
                    mappings[it.id]?.apply {
                        this.quote = it
                    }
                }

                mappings.values.toList()
            }

            _rows.value = result.sortedBy { it.name }

            _positions.value = result.map {
                val amount = when(it.quote?.id) {
                    "ethereum" -> 1.01
                    "bitcoin" -> 0.0685
                    "solana" -> 14.4
                    "matic-network" -> 717.0
                    "algorand" -> 84.0
                    else -> 0.0
                }

                CryptoPosition(it, amount)
            }.filter { it.quantity > 0 }
        }
    }

}