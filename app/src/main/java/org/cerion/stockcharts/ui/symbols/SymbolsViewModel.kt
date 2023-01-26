package org.cerion.stockcharts.ui.symbols

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.cerion.marketdata.webclients.tda.Quote
import org.cerion.marketdata.webclients.tda.TDAmeritrade
import org.cerion.stockcharts.repository.SymbolRepository


enum class SymbolCategory {
    FUND,
    STOCK
}

class SymbolsViewModel(private val symbolRepo: SymbolRepository, private val td: TDAmeritrade) : ViewModel() {

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    private val emptyQuote: Quote? = null

    private val etf = listOf("SPY", "VNQ", "BND", "VXUS", "FXI")
    private val stocks = listOf("ALK", "ARKK", "EMQQ", "LAD", "PBW", "QQQJ", "SQ", "TMUS")
    private lateinit var category: SymbolCategory
    private lateinit var symbols: List<String>

    private val _items = MutableLiveData(emptyMap<String, Quote?>())
    val items: LiveData<Map<String, Quote?>>
        get() = _items

    fun load(category: SymbolCategory) {
        this.category = category
        this.symbols = when(category) {
            SymbolCategory.FUND -> etf
            SymbolCategory.STOCK -> stocks
        }

        _items.value = symbols.associateWith { emptyQuote }

        scope.launch {
            _items.value = withContext(Dispatchers.IO) {
                val quotes = td.getQuotes(symbols)
                quotes.associateBy { it.symbol }
            }
        }
    }

    fun add(symbol: String) {
        /*
        scope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val s = dataApi.getSymbol(symbol)
                    if (s != null) {
                        symbolRepo.add(s)
                        true
                    } else
                        false
                }

                if (success)
                    load()
                else
                    Toast.makeText(getApplication(), "Could not find '$symbol'", Toast.LENGTH_SHORT).show()
            }
            catch(e: Exception) {
                Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
            }
        }
         */
    }

    fun delete(symbol: String?) {
        symbolRepo.delete(symbol!!)
        load(category)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}