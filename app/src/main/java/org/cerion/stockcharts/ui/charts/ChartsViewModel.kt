package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.charts.VolumeChart

class ChartsViewModel : ViewModel() {

    val test = "Placeholder String"

    val charts: LiveData<List<StockChart>> = MutableLiveData<List<StockChart>>(listOf(VolumeChart()))
}