package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import org.cerion.marketdata.core.arrays.FloatArray
import org.cerion.marketdata.core.charts.IndicatorChart
import org.cerion.marketdata.core.charts.PriceChart
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.charts.VolumeChart
import org.cerion.marketdata.core.functions.IIndicator
import org.cerion.marketdata.core.functions.types.Indicator

class EditChartViewModel(val originalChart: StockChart) : ViewModel() {
    interface OnFunctionChangeListener {
        fun onFunctionChanged()
    }

    private val updatedChart = originalChart.copy()

    val showLineCheckbox = originalChart is PriceChart
    val showLogScale = originalChart !is IndicatorChart
    val showFunctions = originalChart is IndicatorChart

    private val functionMap = Indicator.values().map { it.instance }.associateBy { it.name }
    val functions = functionMap.keys.toList().sorted()

    private var mFunctionListener: OnFunctionChangeListener? = null

    private val _showAddOverlay = MutableLiveData(true)
    val showAddOverlay: LiveData<Boolean>
        get() = _showAddOverlay

    val functionIndex = MutableLiveData(0)
    val logScale = MutableLiveData(false)
    val candleStick = MutableLiveData(false)

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
                logScale.value = originalChart.logScale
            }
            is PriceChart -> {
                logScale.value = originalChart.logScale
                candleStick.value = originalChart.candleData
            }
        }

        initFunctions()
    }

    // TODO this clears overlays, need to have those in VM too
    val chart: StockChart
        get() {
            when(updatedChart) {
                is PriceChart -> {
                    updatedChart.logScale = logScale.value!!
                    updatedChart.candleData = candleStick.value!!
                }
                is VolumeChart -> {
                    updatedChart.logScale = logScale.value!!
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
            val name = functions[functionIndex.value!!]
            return functionMap[name]
        }

    override fun onCleared() {
        // TODO is this necessary
        if (functionIndex.hasObservers())
            functionIndex.removeObserver(functionIndexObserver)
    }

    private lateinit var functionIndexObserver: Observer<Int>

    private fun initFunctions() {
        if (originalChart is IndicatorChart) {
            functionIndexObserver = Observer<Int> {
                _showAddOverlay.value = functionAllowOverlays()
                if (mFunctionListener != null)
                    mFunctionListener!!.onFunctionChanged()
            }

            functionIndex.observeForever(functionIndexObserver)

            val index = functions.indexOf(originalChart.indicator.name)
            functionIndex.value = index
        }
    }

    private fun functionAllowOverlays(): Boolean {
        val ii = function!!
        return ii.resultType == FloatArray::class
    }
}