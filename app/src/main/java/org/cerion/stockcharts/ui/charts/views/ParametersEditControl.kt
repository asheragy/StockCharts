package org.cerion.stockcharts.ui.charts.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import org.cerion.stockcharts.R

class ParametersEditControl : LinearLayout {
    private var defaultParameters: Array<Number>? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    // TODO should be non-mutablelist, this sets the fields then GETS a modified version later
    fun setParameters(params: MutableList<Number>) { // TODO add binding so EditText updates corresponding array at all times
        defaultParameters = params.toTypedArray()
        val currentParameters=  defaultParameters!!

        removeAllViews()

        for (i in params.indices) {
            val n = params[i]
            val et = createInputField(n)

            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { // TryParse
                    val defValue = currentParameters[i]
                    val newValue = tryParseNumber(s.toString(), defValue)
                    params[i] = newValue
                }

                override fun afterTextChanged(s: Editable) {}
            })
            addView(et)
        }
    }

    val parameters: Array<Number>
        get() {
            val parameters = findViewById<View>(R.id.parameters) as LinearLayout
            val p = defaultParameters!!.clone()
            check(p.size == parameters.childCount) { "expected parameters do not match layout count" }

            for (i in p.indices) {

                val currField = parameters.getChildAt(i) as EditText
                try {
                    val entered = currField.text.toString()
                    if (p[i] is Int)
                        p[i] = entered.toInt()
                    else
                        p[i] = entered.toFloat()

                } catch (e: Exception) {
                    // TODO may be unnecessary, we are only getting values
                    currField.setText(p[i].toString())
                }
            }

            return p
        }

    private fun tryParseNumber(text: String, defaultVal: Number): Number {
        return try {
            when(defaultVal) {
                is Int -> text.toInt()
                is Float -> text.toFloat()
                else -> throw NotImplementedError()
            }
        }
        catch (e: Exception) {
            defaultVal
        }
    }

    private fun createInputField(n: Number): EditText {
        val input = EditText(context)
        input.setText(n.toString())
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        return input
    }
}