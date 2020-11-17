package org.cerion.stockcharts.ui.charts

import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import org.cerion.stocks.core.arrays.FloatArray
import org.cerion.stocks.core.charts.IndicatorChart
import org.cerion.stocks.core.charts.PriceChart
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.charts.VolumeChart
import org.cerion.stocks.core.functions.IFunction
import org.cerion.stocks.core.functions.IIndicator
import org.cerion.stocks.core.functions.types.Indicator
import java.util.*

class EditChartViewModel(val originalChart: StockChart) {
    interface OnFunctionChangeListener {
        fun onFunctionChanged()
    }

    private val updatedChart = originalChart.copy()

    val showLineCheckbox = originalChart is PriceChart
    val showLogScale = originalChart !is IndicatorChart
    val showFunctions = originalChart is IndicatorChart

    var functions: MutableList<String> = ArrayList()
    private val functionMap: MutableMap<String, IFunction> = HashMap()
    private var mFunctionListener: OnFunctionChangeListener? = null
    var showAddOverlay = ObservableField(true)
    var functionIndex = ObservableField<Int>()
    var logScale = ObservableField<Boolean>()
    var lineChart = ObservableField<Boolean>()

    val title: String
        get() = when(originalChart) {
            is IndicatorChart -> "Indicator"
            is PriceChart -> "Price"
            is VolumeChart -> "Volume"
            else -> "Options"
        }

    init {
        when (originalChart) {
            is VolumeChart -> {
                logScale.set(originalChart.logScale)
            }
            is PriceChart -> {
                logScale.set(originalChart.logScale)
                lineChart.set(!originalChart.candleData)
            }
        }

        initFunctions()
    }

    // TODO this clears overlays, need to have those in VM too
    val chart: StockChart
        get() {
            when(updatedChart) {
                is PriceChart -> {
                    updatedChart.logScale = logScale.get()!!
                    updatedChart.candleData = !lineChart.get()!!
                }
                is VolumeChart -> {
                    updatedChart.logScale = logScale.get()!!
                }
                is IndicatorChart -> {
                    // TODO this clears overlays, need to have those in VM too
                }
            }

            return updatedChart
        }

    fun setFunctionListener(listener: OnFunctionChangeListener?) {
        mFunctionListener = listener
    }

    val function: IIndicator?
        get() {
            val name = functions[functionIndex.get()!!]
            return functionMap[name] as IIndicator?
        }

    private fun initFunctions() {
        functionIndex.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                showAddOverlay.set(functionAllowOverlays())
                if (mFunctionListener != null) mFunctionListener!!.onFunctionChanged()
            }
        })

        if (originalChart is IndicatorChart) {
            val values = Indicator.values()
            for (e in values) {
                val f = e.instance
                functionMap[f.name] = f
                functions.add(f.name)
            }

            functions.sort()

            val index = functions.indexOf(originalChart.indicator.name)
            functionIndex.set(index)
        }
    }

    private fun functionAllowOverlays(): Boolean {
        val ii = function!!
        return ii.resultType == FloatArray::class.java
    }
}