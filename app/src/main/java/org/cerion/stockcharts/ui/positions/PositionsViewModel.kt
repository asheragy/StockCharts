package org.cerion.stockcharts.ui.positions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.TAG
import org.cerion.stocks.core.model.Position
import org.cerion.stocks.core.web.api.TDAmeritrade
import org.cerion.stocks.core.web.api.TDPosition
import java.util.*

class PositionsViewModel : ViewModel() {

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    private val _accounts = MutableLiveData<List<String>>()
    val accounts: LiveData<List<String>>
        get() = _accounts

    private val _positions = MutableLiveData<List<Position>>()
    val positions: LiveData<List<Position>>
        get() = _positions

    val accountIndex = MutableLiveData<Int>()

    val testString: LiveData<String> = Transformations.map(accountIndex) {
        if (it == null)
            "Select Account"
        else
            "Account ${it + 1}"
    }

    init {
        _accounts.value = listOf("Account 1", "Account 2")
        accountIndex.value = 0

        updatePositions()
    }

    private fun updatePositions() {

        scope.launch {

            try {
                val positions = getPositions()
                _positions.value = positions.map {
                    Position(it.symbol, 1.0, it.marketValue, Date(), false)
                }
            }
            catch(e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private suspend fun getPositions(): List<TDPosition> {
        val td = TDAmeritrade("")
        return withContext(Dispatchers.IO) {
            td.getPositions()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
