package org.cerion.stockcharts.ui.charts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.cerion.stockcharts.fakes.FakeAndroidPriceListRepository
import org.cerion.stockcharts.fakes.FakePreferenceRepository
import org.cerion.stockcharts.fakes.FakePriceHistoryDataSource
import org.cerion.stockcharts.fakes.FakePriceListRepository
import org.cerion.stockcharts.repository.AndroidPriceListRepository
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stocks.core.charts.ChartColors
import org.cerion.stocks.core.charts.IndicatorChart
import org.cerion.stocks.core.charts.PriceChart
import org.cerion.stocks.core.charts.VolumeChart
import org.cerion.stocks.core.functions.types.Indicator
import org.cerion.stocks.core.indicators.RSI
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.repository.CachedPriceListRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChartsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var _viewModel: ChartsViewModel
    private lateinit var _prefs: PreferenceRepository
    private lateinit var _cachedRepo: CachedPriceListRepository

    @Before
    fun init() {
        val priceListRepository = FakePriceListRepository()
        val priceHistory = FakePriceHistoryDataSource()
        _cachedRepo = CachedPriceListRepository(priceListRepository, priceHistory)
        _prefs = FakePreferenceRepository()

        _viewModel = ChartsViewModel(_cachedRepo, FakeAndroidPriceListRepository(), _prefs, ChartColors())
    }

    @Test
    fun chartsViewModel_defaults() {
        assertEquals(Interval.DAILY, _viewModel.interval.value)
        assertEquals("^GSPC", _viewModel.symbol.value!!.symbol)
        assertFalse(_viewModel.busy.value!!)
        assertEquals(2, _viewModel.charts.value!!.size)
        assertTrue(_viewModel.charts.value!![0] is PriceChart)
        assertTrue(_viewModel.charts.value!![1] is VolumeChart)
    }

    @Test
    fun chartsViewModel_loadsFromPrefs() {
        _prefs.saveCharts(listOf(VolumeChart()))
        _viewModel = ChartsViewModel(_cachedRepo, FakeAndroidPriceListRepository(), _prefs, ChartColors())

        assertEquals(1, _viewModel.charts.value!!.size)
        assertTrue(_viewModel.charts.value!![0] is VolumeChart)
    }

    @Test
    fun chartsViewModel_savesToPrefs() {
        _viewModel.addPriceChart()
        _viewModel.addIndicatorChart()
        _viewModel.addVolumeChart()

        assertEquals(5, _prefs.getCharts(ChartColors()).size)
    }

    @Test
    fun chartsViewModel_removeChart() {
        val chartToRemove = _viewModel.charts.value!![0]
        _viewModel.removeChart(chartToRemove)

        assertEquals(1, _viewModel.charts.value!!.size)
        assertTrue(_viewModel.charts.value!![0] is VolumeChart)
    }

    @Test
    fun chartsViewModel_replace() {
        val chartToReplace = _viewModel.charts.value!![0]
        val chartToAdd = IndicatorChart(RSI(14))

        _viewModel.replaceChart(chartToReplace, chartToAdd)
        assertEquals(2, _viewModel.charts.value!!.size)
        assertTrue(_viewModel.charts.value!![0] is IndicatorChart)
    }
}