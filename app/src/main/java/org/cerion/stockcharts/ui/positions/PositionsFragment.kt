package org.cerion.stockcharts.ui.positions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.cerion.stockcharts.common.TAG
import org.cerion.stockcharts.databinding.PositionsFragmentBinding

class PositionsFragment : androidx.fragment.app.Fragment() {

    private lateinit var viewModel: PositionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = PositionsFragmentBinding.inflate(layoutInflater)

        viewModel = ViewModelProviders.of(this).get(PositionsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.accountIndex.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "index = $it")
        })

        viewModel.positions.observe(viewLifecycleOwner, Observer {
            binding.chart.setPositions(it)
        })

        return binding.root
    }

}
