package org.cerion.stockcharts.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.cerion.stocks.core.model.Symbol

private const val KEY_LAST_SYMBOL = "lastSymbol"

class PreferenceRepository(context: Context) {

    private val app = context.applicationContext

    private val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(app)

    fun saveLastSymbol(symbol: Symbol) {
        prefs.edit().putString(KEY_LAST_SYMBOL, symbol.symbol + "\t" + symbol.name).apply()
    }

    fun getLastSymbol(): Symbol? {
        val symbol = prefs.getString(KEY_LAST_SYMBOL, null)
        if (symbol != null) {
            val arr = symbol.split("\t")
            return Symbol(arr[0], arr[1])
        }

        return null
    }
}