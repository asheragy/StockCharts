package org.cerion.stockcharts.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import org.cerion.stockcharts.database.SymbolEntity
import org.cerion.stockcharts.database.getSymbolsDatabase


class SymbolSearchAdapter(context: Context) : ArrayAdapter<SymbolEntity>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private val _results = mutableListOf<SymbolEntity>()
    //private val repo = SymbolRepository(context)
    //private val mDatabaseList = SymbolRepository(context).getAll()
    //private val dataApi= Injection.getDataApi()
    private val _lookup = getSymbolsDatabase(context).symbolsDao

    override fun getCount(): Int = _results.size
    override fun getItem(index: Int): SymbolEntity = _results[index]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        }

        val symbol = _results[position]
        (view!!.findViewById<View>(android.R.id.text1) as TextView).text = symbol.symbol + " - " + symbol.name

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                _results.clear()
                if (constraint != null && constraint.isNotEmpty()) {

                    val matches =
                            if (constraint.length >= 3)
                                _lookup.find("$constraint%", "$constraint%")
                            else
                                _lookup.find(constraint.toString()) + _lookup.find(constraint.toString() + "_") // Exact match + matches with 1 other character

                    // Old code to lookup previously saved + api fetch, not using either for now
                    /*
                    //val symbol = dataApi.getSymbol(constraint.toString() + "")
                    //if (symbol != null && symbol.isValid) mResults.add(symbol)
                    for (s in mDatabaseList) {
                        val lookup = constraint.toString() + ""
                        if (s.symbol.startsWith(lookup) && s.symbol.length > lookup.length) {
                            mResults.add(s)
                        }
                    }
                     */

                    _results.addAll(matches)
                }

                filterResults.values = _results
                filterResults.count = _results.size

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