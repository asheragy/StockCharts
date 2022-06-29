package org.cerion.stockcharts.ui.symbols

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.cerion.stockcharts.repository.SymbolRepository
import org.cerion.marketdata.core.model.Symbol

class SymbolsViewModel(private val symbolRepo: SymbolRepository, application: Application) : AndroidViewModel(application) {

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    private val _items = MutableLiveData(emptyList<Symbol>())
    val items: LiveData<List<Symbol>>
        get() = _items

    init {
        load()
    }

    private fun load() {
        scope.launch {
            _items.value = withContext(Dispatchers.IO) {
                symbolRepo.getAll()
            }
        }
    }

    fun add(symbol: String) {
        /*
        scope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val s = dataApi.getSymbol(symbol)
                    if (s != null) {
                        symbolRepo.add(s)
                        true
                    } else
                        false
                }

                if (success)
                    load()
                else
                    Toast.makeText(getApplication(), "Could not find '$symbol'", Toast.LENGTH_SHORT).show()
            }
            catch(e: Exception) {
                Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
            }
        }
         */
    }

    fun delete(symbol: String?) {
        symbolRepo.delete(symbol!!)
        load()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}