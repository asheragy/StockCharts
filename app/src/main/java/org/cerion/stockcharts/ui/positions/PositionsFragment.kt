package org.cerion.stockcharts.ui.positions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.TAG
import org.cerion.stockcharts.databinding.PositionsFragmentBinding

class PositionsFragment : androidx.fragment.app.Fragment() {

    private lateinit var viewModel: PositionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = PositionsFragmentBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this).get(PositionsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.accountIndex.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "index = $it")
        })

        viewModel.positions.observe(viewLifecycleOwner, Observer {
            binding.chart.setPositions(it)
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.positions_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        val uri = requireActivity().intent.data
        if (uri != null)
            viewModel.onAuthCodeResponse(uri)

        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.add -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.tdAuth.authUrlEncoded))
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
