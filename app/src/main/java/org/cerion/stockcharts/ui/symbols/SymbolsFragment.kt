package org.cerion.stockcharts.ui.symbols

import android.app.Fragment
import android.os.Bundle
import android.view.*
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.OnListAnyChangeCallback
import org.cerion.stockcharts.common.SymbolLookupDialogFragment
import org.cerion.stockcharts.common.SymbolLookupDialogFragment.OnSymbolListener
import org.cerion.stocks.core.model.Symbol

class SymbolsFragment : Fragment(), OnSymbolListener {

    private lateinit var viewModel: SymbolsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_symbols, container, false)

        // TODO need to convert fragment to androidx
        //viewModel = ViewModelProvider(this).get(SymbolsViewModel::class.java)
        viewModel = SymbolsViewModel(activity.application)

        val adapter = RecyclerViewAdapter()
        val rv: RecyclerView = view.findViewById(R.id.recycler_view)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = adapter
        setHasOptionsMenu(true)


        viewModel.items.addOnListChangedCallback(object : OnListAnyChangeCallback<ObservableList<Symbol>>() {
            override fun onAnyChange(sender: ObservableList<*>?) {
                adapter.setItems(viewModel.items)
            }
        })

        return view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId == RecyclerViewAdapter.CONTEXT_MENU_GROUP_ID) {
            val s = viewModel.items[item.order]
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
        // TODO replace with navigation and use shared viewmodel to add
        val fm = this.fragmentManager
        val ft = fm.beginTransaction()
        val prev = fm.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }

        ft.addToBackStack(null)
        val dialog = SymbolLookupDialogFragment()
        dialog.setTargetFragment(this, DIALOG_FRAGMENT)
        dialog.show(fm.beginTransaction(), "dialog")
    }

    override fun onSymbolEntered(name: String) {
        viewModel.add(name)
    }

    companion object {
        private const val DIALOG_FRAGMENT = 1
    }
}