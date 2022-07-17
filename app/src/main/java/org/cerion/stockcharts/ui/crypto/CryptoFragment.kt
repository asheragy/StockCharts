package org.cerion.stockcharts.ui.crypto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.cerion.stockcharts.databinding.FragmentCryptoBinding
import org.cerion.stockcharts.ui.FragmentHomeDirections
import org.cerion.stockcharts.ui.symbols.SymbolDetailsFragmentDirections

class CryptoFragment : Fragment() {

    private lateinit var binding: FragmentCryptoBinding
    private val viewModel = CryptoViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCryptoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = CryptoListAdapter(object : CryptoListListener {
            override fun onClick(symbol: String) {
                val action = CryptoFragmentDirections.actionCryptoFragmentToChartsFragment(symbol)
                findNavController().navigate(action)
            }
        })

        viewModel.rows.observe(viewLifecycleOwner) {
            adapter.setRows(it)
        }

        binding.recyclerView.adapter = adapter

        viewModel.load()

        return binding.root
    }
}
