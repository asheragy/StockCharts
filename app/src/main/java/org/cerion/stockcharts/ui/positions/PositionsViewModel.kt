package org.cerion.stockcharts.ui.positions

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
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

    val accountIndex = MutableLiveData(-1)

    val positions = accountIndex.switchMap {index ->
        liveData {
            emit(emptyList())
            if (index >= 0)
                emit(getPositions())
        }
    }

    val testString: LiveData<String> = Transformations.map(accountIndex) {
        if (it == null || it < 0)
            "Select Account"
        else {
            "AccountId ${_accounts.value!![it].id}"
        }
    }

    init {
        _accounts.value = accountRepo.getAll()
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
