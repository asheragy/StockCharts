package org.cerion.stockcharts.ui.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.cerion.stockcharts.databinding.FragmentChartsBinding

class ChartsFragment : Fragment() {

    private lateinit var viewModel: ChartsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChartsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChartsViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        val adapter = ChartListAdapter()
        binding.recyclerView.adapter = adapter

        //adapter.setCharts(viewModel.charts.value!!)

        viewModel.charts.observe(this, Observer {
            adapter.setCharts(it)
        })

        return binding.root
    }

}