package org.cerion.stockcharts.ui.symbols

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.R
import org.cerion.stockcharts.ui.FragmentHomeDirections
import org.cerion.stocks.core.model.Symbol

class SymbolsFragment : Fragment() {

    private lateinit var viewModel: SymbolsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_symbols, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(SymbolsViewModel::class.java)

        val adapter = RecyclerViewAdapter(object : RecyclerViewAdapter.SymbolListener {
            override fun click(symbol: Symbol) {
                val action = FragmentHomeDirections.actionFragmentHomeToSymbolDetailsFragment(symbol.symbol)
                findNavController().navigate(action)
            }
        })

        val rv: RecyclerView = view.findViewById(R.id.recycler_view)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = adapter
        setHasOptionsMenu(true)

        viewModel.items.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        return view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId == RecyclerViewAdapter.CONTEXT_MENU_GROUP_ID) {

            val s = viewModel.items.value!![item.order]
            when (item.itemId) {
                RecyclerViewAdapter.CONTEXT_MENU_DELETE -> viewModel.delete(s.symbol)
                else -> return super.onContextItemSelected(item)
            }
            return true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.symbol_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_symbol -> onAddSymbol()
            else -> return super.onContextItemSelected(item)
        }

        return true
    }

    private fun onAddSymbol() { //DialogFragment newFragment = new SymbolLookupDialogFragment();
        val fm = requireActivity().supportFragmentManager
        val dialog = SymbolLookupDialog()
        dialog.show(fm, SymbolLookupDialog::class.java.simpleName)
    }
}