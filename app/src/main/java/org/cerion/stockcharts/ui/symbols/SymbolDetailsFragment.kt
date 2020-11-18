package org.cerion.stockcharts.ui.symbols

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.cerion.stockcharts.MainActivity
import org.cerion.stockcharts.databinding.FragmentSymbolDetailsBinding
import org.cerion.stockcharts.ui.charts.views.ChartViewFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class SymbolDetailsFragment : Fragment() {

    private val viewModel: SymbolDetailsViewModel by viewModel()
    private lateinit var chartFactory: ChartViewFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSymbolDetailsBinding.inflate(inflater, container, false)

        val args = SymbolDetailsFragmentArgs.fromBundle(requireArguments())
        (requireActivity() as MainActivity).supportActionBar?.title = args.symbol

        chartFactory = ChartViewFactory(requireActivity())
        binding.viewModel = viewModel

        binding.fullscreen.setOnClickListener {
            val action = SymbolDetailsFragmentDirections.actionSymbolDetailsFragmentToChartsFragment(args.symbol)
            findNavController().navigate(action)
        }

        viewModel.prices.observe(viewLifecycleOwner, Observer {
            val chart = chartFactory.getChart(viewModel.chart, it)
            binding.chartFrame.removeAllViews()
            binding.chartFrame.addView(chart)
        })

        viewModel.load(args.symbol)

        return binding.root
    }
}