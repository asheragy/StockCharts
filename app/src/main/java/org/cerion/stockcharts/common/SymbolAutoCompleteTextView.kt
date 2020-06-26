package org.cerion.stockcharts.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView

@SuppressLint("AppCompatCustomView")
class SymbolAutoCompleteTextView(context: Context, attrs: AttributeSet? = null) : AutoCompleteTextView(context, attrs) {

    init {
        threshold = 1 // Some symbols are only 1 character
        val adapter = SymbolSearchAdapter(context)
        setAdapter(adapter)
    }
}