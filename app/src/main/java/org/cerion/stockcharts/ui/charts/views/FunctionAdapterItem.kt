package org.cerion.stockcharts.ui.charts.views

import org.cerion.marketdata.core.functions.IFunction
import org.cerion.marketdata.core.functions.types.IFunctionEnum
import java.util.*

/**
 * Helper class for getting IFunction classes from enums with proper display name and sorting
 */
internal class FunctionAdapterItem private constructor(var function: IFunction) : Comparable<FunctionAdapterItem> {

    override fun toString(): String = function.name
    override fun compareTo(other: FunctionAdapterItem): Int = this.toString().compareTo(other.toString())

    companion object {

        fun getList(values: List<IFunctionEnum>): List<FunctionAdapterItem> {
            val result: MutableList<FunctionAdapterItem> = ArrayList()

            for (e in values)
                result.add(FunctionAdapterItem(e.instance))

            result.sort()
            return result
        }

        fun indexOf(list: List<FunctionAdapterItem>, function: IFunctionEnum): Int {
            return list.indexOfFirst {
                it.function.id == function
            }
        }
    }

}