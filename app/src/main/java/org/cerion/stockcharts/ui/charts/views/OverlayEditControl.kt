package org.cerion.stockcharts.ui.charts.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import org.cerion.stockcharts.R
import org.cerion.stockcharts.ui.charts.views.FunctionAdapterItem.Companion.getList
import org.cerion.stockcharts.ui.charts.views.FunctionAdapterItem.Companion.indexOf
import org.cerion.stockcharts.common.TAG
import org.cerion.marketdata.core.functions.IOverlay
import org.cerion.marketdata.core.functions.types.IFunctionEnum
import org.cerion.marketdata.core.functions.types.Overlay

class OverlayEditControl(context: Context, allowedOverlays: List<IFunctionEnum>) : LinearLayout(context) {

    private var onDeleteListener: OnDeleteListener? = null
    private val spinner: Spinner
    private val overlays: List<FunctionAdapterItem> = getList(allowedOverlays)
    private val parameters: ParametersEditControl

    interface OnDeleteListener {
        fun delete()
    }

    fun setOnDelete(listener: OnDeleteListener) {
        onDeleteListener = listener
    }

    var overlayFunction: IOverlay
        get() {
            val index = spinner.selectedItemPosition
            val function = overlays[index].function as IOverlay
            val params = parameters.parameters
            function.setParams(*params)
            return function
        }
        set(overlay) {
            val index = overlays.indexOfFirst { it.function.id == overlay.id }
            overlays[index].function = overlay
            spinner.setSelection(index)
        }

    init {
        val inflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_overlay_edit_control, this, true)
        parameters = findViewById<View>(R.id.parameters) as ParametersEditControl

        val adapter: ArrayAdapter<FunctionAdapterItem?> = ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, overlays)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = findViewById<View>(R.id.name) as Spinner
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val o = overlays[position].function as IOverlay
                Log.i(TAG, "onSelectOverlay() $o")
                parameters.setParameters(o.params.toMutableList())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<View>(R.id.remove).setOnClickListener {
            onDeleteListener?.delete()
        }

        spinner.setSelection(indexOf(overlays, Overlay.EMA))
    }
}