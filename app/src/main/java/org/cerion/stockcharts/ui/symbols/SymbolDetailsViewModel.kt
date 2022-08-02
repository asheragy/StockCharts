package org.cerion.stockcharts.ui.symbols

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.stockcharts.repository.PriceListSQLRepository
import org.cerion.marketdata.core.charts.PriceChart
import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.marketdata.webclients.FetchInterval

// TODO may need to take the cached repository version
class SymbolDetailsViewModel(private val priceRepo: PriceListSQLRepository) : ViewModel() {

    private val _details = MutableLiveData<SymbolDetails>()
    val details: LiveData<SymbolDetails>
        get() = _details

    private val _table = MutableLiveData<OHLCVTable?>()
    val table: LiveData<OHLCVTable?>
        get() = _table

    val chart = PriceChart()

    fun load(symbol: String) {

        _details.value = SymbolDetails().apply {
            fullName = "Full Name"
            exchangeSymbol = "NYSE: $symbol"
            sector = "Tech"
            marketCap = "10b"
        }

        viewModelScope.launch {

            _table.value = priceRepo.get(symbol, FetchInterval.DAILY).first
        }
    }
}