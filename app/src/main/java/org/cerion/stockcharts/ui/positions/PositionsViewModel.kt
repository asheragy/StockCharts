package org.cerion.stockcharts.ui.positions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.TAG
import org.cerion.stockcharts.repository.Account
import org.cerion.stockcharts.repository.AccountRepository
import org.cerion.stockcharts.repository.PositionRepository
import org.cerion.stocks.core.model.Position
import org.cerion.stocks.core.web.api.TDAmeritrade

class PositionsViewModel : ViewModel() {

    private val accountRepo = AccountRepository()
    private val positionRepo = PositionRepository()
    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>>
        get() = _accounts

    private val _positions = MutableLiveData<List<Position>>()
    val positions: LiveData<List<Position>>
        get() = _positions

    val accountIndex = MutableLiveData(0)

    val testString: LiveData<String> = Transformations.map(accountIndex) {
        if (it == null)
            "Select Account"
        else {
            updatePositions() // TODO not sure this goes here
            "AccountId ${_accounts.value!![it].id}"
        }
    }

    init {
        _accounts.value = accountRepo.getAll()
    }

    private fun updatePositions() {

        scope.launch {
            try {
                _positions.value = getPositions()
            }
            catch(e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private suspend fun getPositions(): List<Position> {
        val td = TDAmeritrade("")
        return withContext(Dispatchers.IO) {
            //td.getPositions()
            positionRepo.getPositionsForAccount(accounts.value!![accountIndex.value!!])
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
