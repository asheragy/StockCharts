package org.cerion.stockcharts.ui.charts

import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.cerion.stockcharts.databinding.FragmentChartsBinding
import org.cerion.stocks.core.charts.StockChart
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChartsFragment : Fragment() {

    private val viewModel: ChartsViewModel by viewModel()
    private lateinit var binding: FragmentChartsBinding
    private lateinit var adapter: ChartListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChartsBinding.inflate(inflater, container, false)

        // TODO add factory method ViewModelProvider(this).get(ChartsViewModel::class.java)

        val args = if (arguments != null) ChartsFragmentArgs.fromBundle(arguments!!) else null
        val symbol = args?.symbol ?: "XLE"
        (requireActivity() as AppCompatActivity).supportActionBar?.title = symbol

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        val chartListener = object : StockChartListener {
            override fun onClick(chart: StockChart) {
                val fm = requireActivity().supportFragmentManager
                val dialog = EditChartDialog.newInstance(chart, viewModel)
                dialog.show(fm, "editDialog")
            }

            override fun onViewPortChange(matrix: Matrix) {
                syncCharts(matrix)
            }
        }

        adapter = ChartListAdapter(requireContext(), chartListener)

        binding.recyclerView.adapter = adapter

        val chartsChangedObserver = Observer<Any> {
            adapter.setCharts(viewModel.charts.value!!, viewModel.prices.value)
        }

        viewModel.charts.observe(viewLifecycleOwner, chartsChangedObserver)
        viewModel.prices.observe(viewLifecycleOwner, chartsChangedObserver)

        binding.fabGroup.add("Price") { viewModel.addPriceChart() }
        binding.fabGroup.add("Volume") { viewModel.addVolumeChart() }
        binding.fabGroup.add("Indicator") { viewModel.addIndicatorChart() }

        viewModel.load(symbol)

        return binding.root
    }

    private val mainVals = FloatArray(9)
    private fun syncCharts(matrix: Matrix) {
        matrix.getValues(mainVals)

        binding.recyclerView.let {
            val lm = binding.recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = lm.findLastVisibleItemPosition()

            for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                val holder = it.findViewHolderForAdapterPosition(i)
                adapter.syncMatrix(matrix, mainVals, holder as ChartListAdapter.ViewHolder)
            }
        }
    }
}