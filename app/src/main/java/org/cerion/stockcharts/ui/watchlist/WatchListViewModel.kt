package org.cerion.stockcharts.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cerion.marketdata.core.functions.conditions.Condition
import org.cerion.marketdata.core.functions.conditions.IndicatorCondition
import org.cerion.marketdata.core.functions.conditions.PriceCondition
import org.cerion.marketdata.core.overlays.SimpleMovingAverage
import org.cerion.stockcharts.repository.CachedPriceListRepository

class WatchListViewModel(private val repo: CachedPriceListRepository) : ViewModel() {

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    private val _items = MutableLiveData(emptyList<WatchItemViewModel>())
    val items: LiveData<List<WatchItemViewModel>>
        get() = _items

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