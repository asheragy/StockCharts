package org.cerion.stockcharts.ui.symbols

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.cerion.stockcharts.MainActivity
import org.cerion.stockcharts.databinding.FragmentSymbolDetailsBinding

class SymbolDetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSymbolDetailsBinding.inflate(inflater, container, false)

        val args = SymbolDetailsFragmentArgs.fromBundle(arguments!!)
        (requireActivity() as MainActivity).supportActionBar?.title = args.symbol


        binding.chart.rootView.setOnClickListener {
            val action = SymbolDetailsFragmentDirections.actionSymbolDetailsFragmentToChartsFragment(args.symbol)
            findNavController().navigate(action)
        }

        return binding.root
    }
}