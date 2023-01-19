package org.cerion.stockcharts.ui.charts

import android.graphics.Matrix
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.core.view.MenuCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.model.Interval
import org.cerion.marketdata.core.model.Symbol
import org.cerion.stockcharts.R
import org.cerion.stockcharts.appCompatActivity
import org.cerion.stockcharts.common.SymbolSearchView
import org.cerion.stockcharts.database.getDatabase
import org.cerion.stockcharts.databinding.FragmentChartsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChartsFragment : Fragment() {

    private val viewModel: ChartsViewModel by viewModel()
    private lateinit var binding: FragmentChartsBinding
    private lateinit var adapter: ChartListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChartsBinding.inflate(inflater, container, false)

        appCompatActivity?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        val chartListener = object : StockChartListener {
            override fun onClick(chart: StockChart) {
                viewModel.editChart(chart)
            }

            override fun onViewPortChange(matrix: Matrix) {
                syncCharts(matrix)
            }
        }

        adapter = ChartListAdapter(requireContext(), chartListener)
        binding.recyclerView.adapter = adapter

        val chartsChangedObserver = Observer<Any?> {
            var intervals = 0
            viewModel.rangeSelect.value?.getContentIfNotHandled()?.also {
                intervals = it
            }

            adapter.setCharts(viewModel.charts.value!!, viewModel.table.value, intervals)
        }

        viewModel.busy.observe(viewLifecycleOwner) {
            binding.loadingBar.visibility = if(it) View.VISIBLE else View.GONE
        }

        viewModel.symbol.observe(viewLifecycleOwner) {
            appCompatActivity?.supportActionBar?.title = it.symbol
            binding.title.text = it.name
        }

        // Intervals
        binding.interval.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                viewModel.interval.value = Interval.values()[position]
            }
        }

        viewModel.ranges.observe(viewLifecycleOwner) {
            it.forEachIndexed { index, label ->
                val chip = binding.ranges[index] as Chip
                chip.text = label
            }
        }

        binding.ranges.children.forEachIndexed { index, view ->
            view as Chip
            view.setOnClickListener {
                viewModel.setRange(index)
            }
        }

        viewModel.editChart.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { chart ->
                val fm = requireActivity().supportFragmentManager
                val dialog = EditChartDialog.newInstance(chart, viewModel)
                dialog.show(fm, "editDialog")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.charts.observe(viewLifecycleOwner, chartsChangedObserver)
        viewModel.table.observe(viewLifecycleOwner, chartsChangedObserver)
        viewModel.rangeSelect.observe(viewLifecycleOwner, chartsChangedObserver)

        if (savedInstanceState == null) {
            if (arguments != null) {
                val str = ChartsFragmentArgs.fromBundle(requireArguments()).symbol
                val symbol = Symbol(str)
                viewModel.load(symbol)
            }
            else
                viewModel.load()
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.charts_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SymbolSearchView

        searchView.setOnSymbolClickListener(object : SymbolSearchView.OnSymbolClickListener {
            override fun onClick(symbol: Symbol) {
                viewModel.load(symbol)
                menuItem.collapseActionView()
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_indicator -> viewModel.addIndicatorChart()
            R.id.add_price -> viewModel.addPriceChart()
            R.id.add_volume -> viewModel.addVolumeChart()
            R.id.clear_cache -> viewModel.clearCache()
            R.id.stats -> showStats()
            else -> return super.onContextItemSelected(item)
        }

        return true
    }

    private val _mainVals = FloatArray(9)
    private fun syncCharts(matrix: Matrix) {
        matrix.getValues(_mainVals)
        for(view in binding.recyclerView.children) {
            adapter.syncMatrix(matrix, _mainVals, binding.recyclerView.getChildViewHolder(view) as ChartListAdapter.ViewHolder)
        }
    }

    // Debug only
    private fun showStats() {
        val db = getDatabase(requireContext())
        val name = getDatabase(requireContext()).openHelper.databaseName
        val file = requireContext().getDatabasePath(name)
        val sizeInKb = file.length() / 1024

        //Log.i("Main", "Size before compact: ${file.length()}")
        val lists = db.priceListDao.getAll()

        Toast.makeText(requireContext(), "${lists.size} lists with size ${sizeInKb}kb", Toast.LENGTH_LONG).show()
    }
}