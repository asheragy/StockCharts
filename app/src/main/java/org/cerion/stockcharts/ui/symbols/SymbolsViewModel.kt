package org.cerion.stockcharts.ui.symbols

import android.app.Application
import android.widget.Toast
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.*
import org.cerion.stockcharts.Injection
import org.cerion.stockcharts.repository.SymbolRepository
import org.cerion.stocks.core.model.Symbol

class SymbolsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SymbolRepository(application)
    private val dataApi = Injection.getAPI(application)

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    val items: ObservableList<Symbol> = ObservableArrayList()

    init {
        load()
    }

    private fun load() {
        scope.launch {
            val symbols = withContext(Dispatchers.IO) {
                repo.getAll()
            }

            items.clear()
            items.addAll(symbols)
        }
    }

    fun add(symbol: String) {
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                val s = dataApi.getSymbol(symbol)
                if (s != null) {
                    repo.add(s)
                    true
                }
                else
                    false
            }

            if(success)
                load()
            else
                Toast.makeText(getApplication(), "Could not find '$symbol'", Toast.LENGTH_SHORT).show()
        }
    }

    fun delete(symbol: String?) {
        repo.delete(symbol!!)
        load()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}