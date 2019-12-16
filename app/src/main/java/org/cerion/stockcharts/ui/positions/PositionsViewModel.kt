package org.cerion.stockcharts.ui.positions

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cerion.stockcharts.BuildConfig
import org.cerion.stockcharts.common.TAG
import org.cerion.stockcharts.database.Account
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stockcharts.repository.AccountRepository
import org.cerion.stockcharts.repository.PositionRepository
import org.cerion.stocks.core.model.Position
import org.cerion.stocks.core.web.api.TDAmeritradeAuth

class PositionsViewModel(application: Application) : AndroidViewModel(application) {

    private val accountDao = getDatabase(getApplication()).accountDao
    private val accountRepo = AccountRepository(accountDao)
    private val positionRepo = PositionRepository(accountDao)

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    val tdAuth = TDAmeritradeAuth(BuildConfig.CONSUMER_KEY, BuildConfig.REDIRECT_URI)

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

    fun onAuthCodeResponse(responseUri: Uri) {
        if(responseUri.toString().startsWith(BuildConfig.REDIRECT_URI)) {
            val code = responseUri.getQueryParameter("code")
            if (code != null) {

                scope.launch {
                    authorizeWithCode(code)
                }

            }

            // TODO handle error cases
        }
    }

    private suspend fun authorizeWithCode(code: String) {
        withContext(Dispatchers.IO) {
            val response = tdAuth.authorize(code)
            Log.e(TAG, response.toString())

            val dao = getDatabase(getApplication()).accountDao
            dao.insert(Account("TD", response.refreshToken!!, response.accessToken))
        }
    }

    private suspend fun getPositions(): List<Position> {
        return withContext(Dispatchers.IO) {
            val acct = accounts.value!![accountIndex.value!!]
            positionRepo.getPositionsForAccount(acct)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
