package org.cerion.stockcharts.ui.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import org.cerion.marketdata.core.charts.IndicatorChart
import org.cerion.marketdata.core.charts.PriceChart
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.functions.IIndicator
import org.cerion.marketdata.core.functions.ISimpleOverlay
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.BindingUtils
import org.cerion.stockcharts.databinding.DialogChartEditBinding
import org.cerion.stockcharts.ui.charts.views.OverlayEditControl
import org.cerion.stockcharts.ui.charts.views.ParametersEditControl

class EditChartDialog : DialogFragment(), EditChartViewModel.OnFunctionChangeListener {

    companion object {
        fun newInstance(chart: StockChart, chartsViewModel: ChartsViewModel): EditChartDialog {
            val dialog = EditChartDialog()
            dialog.viewModel = EditChartViewModel(chart)
            dialog.chartsViewModel = chartsViewModel
            return dialog
        }
    }

    private lateinit var viewModel: EditChartViewModel
    private lateinit var binding: DialogChartEditBinding
    private lateinit var overlays: LinearLayout
    private lateinit var chartsViewModel: ChartsViewModel

    private val chart: StockChart
        get() = viewModel.chart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_chart_edit, container)

        binding = DialogChartEditBinding.bind(view)
        binding.title.text = viewModel.title
        binding.checkLogscale.visibility = if(viewModel.showLogScale) View.VISIBLE else View.GONE
        binding.checkCandlestick.visibility = if(viewModel.showLineCheckbox) View.VISIBLE else View.GONE
        binding.function.visibility = if(viewModel.showFunctions) View.VISIBLE else View.GONE

        viewModel.showAddOverlay.observe(viewLifecycleOwner) {
            binding.overlaysSection.visibility = if(it) View.VISIBLE else View.GONE
        }

        BindingUtils.setTwoWayBinding(viewLifecycleOwner, binding.checkLogscale, viewModel.logScale)
        BindingUtils.setTwoWayBinding(viewLifecycleOwner, binding.checkCandlestick, viewModel.candleStick)

        binding.addOverlay.setOnClickListener { onAddOverlay() }

        binding.remove.setOnClickListener {
            chartsViewModel.removeChart(viewModel.originalChart)
            dismiss()
        }

        binding.save.setOnClickListener {
            updateChart()
            chartsViewModel.replaceChart(viewModel.originalChart, chart)
            dismiss()
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }

        if (viewModel.showFunctions) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewModel.functions)
            binding.function.adapter = adapter

            binding.function.setSelection(viewModel.functionIndex.value!!)
            binding.function.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.functionIndex.value = position
                }
            }

            viewModel.functionIndex.observe(viewLifecycleOwner) {
                binding.function.setSelection(it)
            }
        }

        overlays = binding.overlays

        overlays.removeAllViews() // remove placeholder used in design viewer
        viewModel.setFunctionListener(this@EditChartDialog)
        if (chart is IndicatorChart) {
            setIndicator((chart as IndicatorChart).indicator)
        }
        for (i in 0 until viewModel.chart.overlayCount) {
            val overlay = viewModel.chart.getOverlay(i)
            onAddOverlay().overlayFunction = overlay
        }
        return view
    }

    /*
    @Override
    public void onPause() {
        super.onPause();

        // Overlays are not working with binding yet so manually update them before rotation
        updateChart();
    }
    */

    private fun updateChart() {
        val chart = viewModel.chart
        chart.clearOverlays()
        // TODO overlays and parameters all need to be in viewmodel

        if (chart is IndicatorChart) {
            val instance = viewModel.function!!
            instance.setParams(*parametersControl.parameters)
            indicatorChart().indicator = instance
        }

        // Get overlay parameters
        for (i in 0 until overlays.childCount) {
            val editControl = overlays.getChildAt(i) as OverlayEditControl
            if (chart is PriceChart) {
                chart.addOverlay(editControl.overlayFunction)
            } else chart.addOverlay((editControl.overlayFunction as ISimpleOverlay))
        }
    }

    override fun onDestroyView() { // Don't dismiss on rotate
        if (retainInstance)
            dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    private fun indicatorChart(): IndicatorChart {
        return viewModel!!.chart as IndicatorChart
    }

    override fun onFunctionChanged() {
        setIndicator(viewModel.function!!)
    }

    private fun setIndicator(instance: IIndicator) {
        if (indicatorChart().indicator !== instance) indicatorChart().indicator = instance
        // If overlay is not allowed then hide it
        if (!viewModel.showAddOverlay.value!!) {
            overlays.removeAllViews()
        }
        // Add parameters
        val params = instance.params
        parametersControl.setParameters(params.toMutableList())
    }

    private val parametersControl: ParametersEditControl
        get() {
            if (chart is IndicatorChart) return binding!!.parameters
            throw RuntimeException()
        }

    private fun onAddOverlay(): OverlayEditControl {
        val control = OverlayEditControl(requireContext(), viewModel.chart.overlays)

        control.setOnDelete(object : OverlayEditControl.OnDeleteListener {
            override fun delete() {
                overlays.removeView(control)
            }

        })

        overlays.addView(control)
        return control
    }
}