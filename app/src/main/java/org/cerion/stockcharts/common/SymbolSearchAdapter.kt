package org.cerion.stockcharts.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import org.cerion.stockcharts.Injection
import org.cerion.stockcharts.repository.SymbolRepository
import org.cerion.stocks.core.model.Symbol


class SymbolSearchAdapter(context: Context) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private val mResults = mutableListOf<Symbol>()
    private val repo = SymbolRepository(context)
    private val mDatabaseList = SymbolRepository(context).getAll()
    private val dataApi= Injection.getDataApi()

    override fun getCount(): Int = mResults.size
    override fun getItem(index: Int): String = mResults[index].symbol

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        }

        val symbol = mResults[position]
        (view!!.findViewById<View>(android.R.id.text1) as TextView).text = symbol.symbol + " - " + symbol.name

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null) {
                    mResults.clear()
                    val symbol = dataApi.getSymbol(constraint.toString() + "")
                    if (symbol != null && symbol.isValid) mResults.add(symbol)
                    for (s in mDatabaseList) {
                        val lookup = constraint.toString() + ""
                        if (s.symbol.startsWith(lookup) && s.symbol.length > lookup.length) {
                            mResults.add(s)
                        }
                    }
                    filterResults.values = mResults
                    filterResults.count = mResults.size
                }
                return filterResults
            }

            override fun publishResults(contraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0)
                    notifyDataSetChanged()
                else
                    notifyDataSetInvalidated()
            }
        }
    }
}