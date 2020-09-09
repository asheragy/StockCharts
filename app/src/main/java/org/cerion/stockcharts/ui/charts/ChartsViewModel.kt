package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.Event
import org.cerion.stockcharts.repository.AndroidPriceListRepository
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.*
import org.cerion.stocks.core.indicators.AccumulationDistributionLine
import org.cerion.stocks.core.indicators.MACD
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.model.Symbol
import org.cerion.stocks.core.overlays.BollingerBands
import org.cerion.stocks.core.overlays.ExpMovingAverage
import org.cerion.stocks.core.overlays.ParabolicSAR
import org.cerion.stocks.core.overlays.SimpleMovingAverage
import org.cerion.stocks.core.repository.CachedPriceListRepository
import org.koin.ext.scope

class ChartsViewModel(
        private val repo: CachedPriceListRepository,
        private val sqlRepo: AndroidPriceListRepository,
        private val prefs: PreferenceRepository,
        private val colors: ChartColors) : ViewModel() {

    private val DefaultSymbol = Symbol("^GSPC", "S&P 500")

    private val _symbol = MutableLiveData(DefaultSymbol)
    val symbol: LiveData<Symbol>
        get() = _symbol

    private val _interval = MutableLiveData(Interval.DAILY)
    val interval: LiveData<Interval>
        get() = _interval

    private val _editChart = MutableLiveData<Event<StockChart>>()
    val editChart: LiveData<Event<StockChart>>
        get() = _editChart

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>>
        get() = _error

    val prices = MediatorLiveData<PriceList?>()

    private val DefaultCharts = mutableListOf(
            PriceChart(colors),
            VolumeChart(colors))

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

    private var _charts = mutableListOf<StockChart>()
    val charts: MutableLiveData<List<StockChart>> = MutableLiveData(_charts)

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    private var cleanupCache = true

    init {
        // Load saved charts
        _charts.addAll(prefs.getCharts(colors))
        if (_charts.isEmpty())
            _charts.addAll(DefaultChartsTest)

        charts.value = _charts
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
            if (prices.value != null)
                prefs.addSymbolHistory(symbol)
        }
    }

    fun setInterval(interval: Interval) {
        _interval.value = interval
        viewModelScope.launch {
            refresh()
        }
    }

    private suspend fun refresh() {
        runBusy {
            // On the 2nd fetch of this app instance, cleanup database if needed
            if (prices.value != null && cleanupCache) {
                cleanupCache = false
                sqlRepo.cleanupCache()
            }

            val symbol = _symbol.value!!.symbol
            try {
                prices.value = getPricesAsync(symbol).await()
            } catch (e: Exception) {
                _error.value = Event(e.message ?: "Failed to load $symbol")
                prices.value = null
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

    private suspend fun getPricesAsync(symbol: String): Deferred<PriceList> {
        return withContext(Dispatchers.IO) {
            async(Dispatchers.IO) {
                repo.get(symbol, interval.value!!)
            }
        }
    }
}
