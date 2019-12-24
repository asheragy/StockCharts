package org.cerion.stockcharts.ui.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.cerion.stockcharts.Injection
import org.cerion.stockcharts.databinding.FragmentChartsBinding

class ChartsFragment : Fragment() {

    private lateinit var viewModel: ChartsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChartsBinding.inflate(inflater, container, false)

        viewModel = ChartsViewModel(Injection.getAPI(requireContext()))
                // TODO add factory method ViewModelProvider(this).get(ChartsViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        val adapter = ChartListAdapter(requireContext())
        binding.recyclerView.adapter = adapter

        val chartsChangedObserver = Observer<Any> {
            adapter.setCharts(viewModel.charts.value!!, viewModel.prices.value)
        }

        viewModel.charts.observe(viewLifecycleOwner, chartsChangedObserver)
        viewModel.prices.observe(viewLifecycleOwner, chartsChangedObserver)


        viewModel.interval.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), "interval changed", Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }
}