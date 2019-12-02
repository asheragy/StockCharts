package org.cerion.stockcharts.ui.positions

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.cerion.stockcharts.R

class PositionsFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = PositionsFragment()
    }

    private lateinit var viewModel: PositionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.positions_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PositionsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
