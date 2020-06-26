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

class SymbolSearchView(context: Context, attrs: AttributeSet?) : SearchView(context, attrs) {

    private val _searchAutoComplete: SearchAutoComplete = findViewById<View>(R.id.search_src_text) as SearchAutoComplete
    private val _adapter = SymbolSearchAdapter(context)

    constructor(context: Context) : this(context, null)

    init {
        setAdapter(_adapter)
        _searchAutoComplete.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        @SuppressLint("RestrictedApi")
        _searchAutoComplete.threshold = 1;
        setOnItemClickListener(null)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        _searchAutoComplete!!.onItemClickListener = listener
    }

    private fun setAdapter(adapter: ArrayAdapter<*>?) {
        _searchAutoComplete!!.setAdapter(adapter)
    }

    fun setText(text: String?) {
        _searchAutoComplete.setText(text)
    }
}