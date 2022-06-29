package org.cerion.stockcharts.ui.watchlist

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cerion.stockcharts.Injection
import org.cerion.marketdata.core.functions.conditions.Condition
import org.cerion.marketdata.core.functions.conditions.IndicatorCondition
import org.cerion.marketdata.core.functions.conditions.PriceCondition
import org.cerion.marketdata.core.overlays.SimpleMovingAverage

class WatchListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Injection.getPriceListRepository(application)
    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    private val _items = MutableLiveData(emptyList<WatchItemViewModel>())
    val items: LiveData<List<WatchItemViewModel>>
        get() = _items

    val loading = ObservableField(false)

    fun load() {
        // TODO load items from database
    }

    init {
        _items.value = listOf(
                WatchItemViewModel(repo, PriceCondition(Condition.ABOVE, SimpleMovingAverage(225)), "^GSPC"),
                WatchItemViewModel(repo, IndicatorCondition(SimpleMovingAverage(35), Condition.BELOW, SimpleMovingAverage(225)), "^GSPC"),
                WatchItemViewModel(repo, IndicatorCondition(SimpleMovingAverage(35), Condition.ABOVE, SimpleMovingAverage(225)), "^GSPC"))

        scope.launch {
            for(item in items.value!!)
                item.update()
        }
    }

}