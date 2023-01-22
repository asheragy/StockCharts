package org.cerion.stockcharts.ui.crypto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import org.cerion.stockcharts.databinding.FragmentCryptoBinding
import org.cerion.stockcharts.ui.HomeFragmentDirections


class CryptoFragment : Fragment() {

    private lateinit var binding: FragmentCryptoBinding
    private val viewModel = CryptoViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCryptoBinding.inflate(inflater, container, false)

        val adapter = CryptoListAdapter(object : CryptoListListener {
            override fun onClick(symbol: String) {
                val action = HomeFragmentDirections.actionFragmentHomeToChartsFragment(symbol)
                findNavController().navigate(action)
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.load()
        }

        viewModel.rows.observe(viewLifecycleOwner) {
            adapter.setRows(it)
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.positions.observe(viewLifecycleOwner) {
            binding.chart.setPositions(it)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))


        viewModel.load()

        return binding.root
    }
}
