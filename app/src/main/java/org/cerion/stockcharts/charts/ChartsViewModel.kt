package org.cerion.stockcharts.charts

import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import org.cerion.stockcharts.common.Constants
import org.cerion.stockcharts.common.FloatingActionButtonGroup.FabStateListener
import org.cerion.stockcharts.common.GenericAsyncTask
import org.cerion.stockcharts.common.GenericAsyncTask.TaskHandler
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.PriceRow
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.web.CachedDataAPI
import java.util.*

class ChartsViewModel(val symbol: String, private val api: CachedDataAPI, private val view: ChartsView) : FabStateListener {
    val interval = ObservableField(Interval.DAILY)
    val priceList = ObservableField<PriceList?>()
    var charts: List<ChartViewModel> = ArrayList()
    val loading = ObservableField<Boolean>()
    val fabOpen = ObservableField(false)
    val fullName = ObservableField("")

    interface ChartsView {
        fun onErrorLoading(error: String?)
    }

    fun setInterval(interval: Interval) {
        this.interval.set(interval)
    }

    fun loadData() {
        loading.set(true)
        val task = GenericAsyncTask(object : TaskHandler {
            var result: PriceList? = null
            var error: Exception? = null
            override fun run() {
                var prices: List<PriceRow>? = null
                try {
                    when (interval.get()) {
                        Interval.DAILY -> prices = api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY)
                        Interval.WEEKLY -> prices = api.getPrices(symbol, Interval.WEEKLY, Constants.START_DATE_WEEKLY)
                        Interval.MONTHLY -> prices = api.getPrices(symbol, Interval.MONTHLY, Constants.START_DATE_MONTHLY)
                        Interval.QUARTERLY -> prices = api.getPrices(symbol, Interval.MONTHLY, Constants.START_DATE_MONTHLY)
                    }
                    result = PriceList(symbol, prices!!)

                    if (interval.get() === Interval.QUARTERLY) result = result!!.toQuarterly()
                } catch (e: Exception) {
                    error = e
                }
            }

            override fun onFinish() {
                if (error == null) priceList.set(result) else view.onErrorLoading(error!!.message)
                loading.set(false)
            }
        })
        task.execute()
    }

    override fun onStateChange(open: Boolean) {
        fabOpen.set(open)
    }

    init {
        loadData()
        interval.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                loadData()
            }
        })
    }
}