package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.Event
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.marketdata.core.charts.*
import org.cerion.marketdata.core.indicators.MACD
import org.cerion.marketdata.core.model.Interval
import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.marketdata.core.model.Symbol
import org.cerion.stockcharts.repository.CachedPriceListRepository
import org.cerion.stockcharts.repository.PriceListRepository


class ChartsViewModel(
    private val repo: CachedPriceListRepository,
    private val sqlRepo: PriceListRepository,
    private val prefs: PreferenceRepository,
    private val colors: ChartColors) : ViewModel() {

    private val DefaultSymbol = Symbol("^GSPC", "S&P 500")

    private val _symbol = MutableLiveData(DefaultSymbol)
    val symbol: LiveData<Symbol>
        get() = _symbol

    val intervals = listOf(Interval.DAILY, Interval.WEEKLY, Interval.MONTHLY, Interval.QUARTERLY)
    val interval = MutableLiveData(Interval.DAILY)

    private val _editChart = MutableLiveData<Event<StockChart>>()
    val editChart: LiveData<Event<StockChart>>
        get() = _editChart

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>>
        get() = _error

    val table = MediatorLiveData<OHLCVTable?>()

    private val DefaultCharts = mutableListOf(
            PriceChart(colors),
            VolumeChart(colors))

    /*
    private val DefaultChartsTest = mutableListOf(
            PriceChart(colors).apply {
                addOverlay(BollingerBands())
                addOverlay(SimpleMovingAverage())
                addOverlay(ExpMovingAverage())
            },
            VolumeChart(colors),
            IndicatorChart(MACD(), colors),
            PriceChart(colors).apply {
                addOverlay(ParabolicSAR())
            },
            VolumeChart(colors),
            IndicatorChart(AccumulationDistributionLine(), colors)
    )
     */

    private var _charts = mutableListOf<StockChart>()
    val charts: MutableLiveData<List<StockChart>> = MutableLiveData(_charts)

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    private var cleanupCache = true

    val rangeSelect = MutableLiveData<Event<Int>>()
    val ranges = Transformations.map(interval) {
        when(it) {
            Interval.DAILY -> listOf("1M", "6M", "1Y", "MAX")
            Interval.WEEKLY -> listOf("3M", "1Y", "5Y", "MAX")
            Interval.MONTHLY -> listOf("1Y", "5Y", "10Y", "MAX")
            else -> listOf("3Y", "5Y", "10Y", "MAX")
        }
    }

    init {
        // Load saved charts
        _charts.addAll(prefs.getCharts(colors))
        if (_charts.isEmpty())
            _charts.addAll(DefaultCharts)

        charts.value = _charts

        table.addSource(interval) {
            // Refresh only if prices are already loaded and interval was changed
            if (table.value != null && table.value!!.interval != it) {
                viewModelScope.launch {
                    refresh()
                }
            }
        }
    }

    fun load() {
        val lastSymbol = prefs.getSymbolHistory().lastOrNull()
        if (lastSymbol != null)
            _symbol.value = lastSymbol

        viewModelScope.launch {
            refresh()
        }
    }

    fun load(symbol: Symbol) {
        _symbol.value = symbol

        viewModelScope.launch {
            refresh()
            // If successfully loaded prices save symbol history
            if (table.value != null)
                prefs.addSymbolHistory(symbol)
        }
    }

    fun setRange(position: Int) {
        val range: Int = when(interval.value) {
            // TODO daily is wrong with crypto
            // Logic should be built in charts
            Interval.DAILY -> when(position) {
                    0 -> 30 // 1 month
                    1 -> 125 // 6 month
                    2 -> 250 // year
                    else -> 0
                }
            Interval.WEEKLY -> when(position) {
                    0 -> 12 // 3 month
                    1 -> 50 // year
                    2 -> 50 * 5 // 5Y
                    else -> 0
                }
            Interval.MONTHLY -> when(position) {
                0 -> 12 // 1Y
                1 -> 12*5 // 5Y
                2 -> 12*10 // 10Y
                else -> 0
            }
            else -> 4 * when(position) { // Quarterly
                0 -> 3
                1 -> 5
                2 -> 10
                else -> 0
            }
        }

        rangeSelect.value = Event(range)
    }

    private suspend fun refresh() {
        runBusy {
            // On the 2nd fetch of this app instance, cleanup database if needed
            if (table.value != null && cleanupCache) {
                cleanupCache = false
                sqlRepo.cleanupCache()
            }

            val symbol = _symbol.value!!.symbol
            try {
                table.value = getPricesAsync(symbol).await()
            } catch (e: Exception) {
                _error.value = Event(e.message ?: "Failed to load $symbol")
                table.value = null
            }
        }
    }

    private suspend fun runBusy(block: suspend () -> Unit) {
        try {
            _busy.value = true
            block()
        } finally {
            _busy.value = false
        }
    }

    fun editChart(chart: StockChart) {
        _editChart.value = Event(chart)
    }

    fun addPriceChart() {
        addChart(PriceChart(colors).apply {
            candleData = false
        })
    }

    fun addIndicatorChart() {
        val newChart = IndicatorChart(MACD(), colors)
        addChart(newChart)
        editChart(newChart)
    }

    fun addVolumeChart() {
        addChart(VolumeChart(colors))
    }

    fun removeChart(chart: StockChart) {
        _charts.remove(chart)
        charts.value = _charts
        saveCharts()
    }

    fun replaceChart(old: StockChart, new: StockChart) {
        _charts = _charts.map { if (it == old) new else it }.toMutableList()
        charts.value = _charts
        saveCharts()
    }

    fun clearCache() {
        viewModelScope.launch {
            runBusy {
                sqlRepo.clearCache()
            }
        }
    }

    private fun saveCharts() {
        prefs.saveCharts(_charts)
    }

    private fun addChart(chart: StockChart) {
        _charts.add(chart)
        charts.value = _charts
        saveCharts()
    }

    private suspend fun getPricesAsync(symbol: String): Deferred<OHLCVTable> {
        interval.value
        return withContext(Dispatchers.IO) {
            async(Dispatchers.IO) {
                repo.get(symbol, interval.value!!)
            }
        }
    }
}
