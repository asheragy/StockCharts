package org.cerion.stockcharts.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.cerion.stockcharts.databinding.FragmentWatchlistBinding

class WatchListFragment : Fragment(), View.OnClickListener {

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var binding: FragmentWatchlistBinding
    private lateinit var viewModel: WatchListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(WatchListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.setOnClickListener(this)

        // TODO load data here when in database
        // TODO show like this http://www.marketwatch.com/watchlist
        adapter = RecyclerViewAdapter()
        binding.recyclerView.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        return binding.root
    }

    override fun onClick(view: View) {
        // TODO change to function passed into adapter
        val position = binding.recyclerView.getChildLayoutPosition(view)
        val item = viewModel.items.value!![position]
        //Toast.makeText(context, item.condition, Toast.LENGTH_LONG).show()
    }
}