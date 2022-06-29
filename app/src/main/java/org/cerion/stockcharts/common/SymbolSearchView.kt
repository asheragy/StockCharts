package org.cerion.stockcharts.common

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.appcompat.R
import androidx.appcompat.widget.SearchView
import org.cerion.stockcharts.database.toSymbol
import org.cerion.marketdata.core.model.Symbol

class SymbolSearchView(context: Context, attrs: AttributeSet?) : SearchView(context, attrs) {

    interface OnSymbolClickListener {
        fun onClick(symbol: Symbol)
    }

    private val _searchAutoComplete: SearchAutoComplete = findViewById<View>(R.id.search_src_text) as SearchAutoComplete
    private val _adapter = SymbolSearchAdapter(context)
    private var _listener: OnSymbolClickListener? = null

    constructor(context: Context) : this(context, null)

    init {
        setAdapter(_adapter)
        _searchAutoComplete.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        @SuppressLint("RestrictedApi")
        _searchAutoComplete.threshold = 0 // Previous history is shown if nothing is entered

        _searchAutoComplete.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val symbol = _adapter.getItem(position).toSymbol()
            _listener?.onClick(symbol)
        }
    }

    fun setOnSymbolClickListener(listener: OnSymbolClickListener) {
        _listener = listener
    }

    private fun setAdapter(adapter: ArrayAdapter<*>?) {
        _searchAutoComplete.setAdapter(adapter)
    }
}