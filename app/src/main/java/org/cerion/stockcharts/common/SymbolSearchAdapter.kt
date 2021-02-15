package org.cerion.stockcharts.common

import android.annotation.SuppressLint
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
import org.cerion.stockcharts.repository.DefaultPreferenceRepository


class SymbolSearchAdapter(context: Context) : ArrayAdapter<SymbolEntity>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private val _results = mutableListOf<SymbolEntity>()
    private val _lookup = getSymbolsDatabase(context).symbolsDao
    private val _prefs = DefaultPreferenceRepository(context)
    private val _crypto = mutableListOf<SymbolEntity>()

    init {
        val coins = mapOf(
                Pair("BTC-USD", "Bitcoin"),
                Pair("ETH-USD", "Ethereum"),
                Pair("LTC-USD", "Litecoin"),
                Pair("USDT-USD", "Tether"),
                Pair("DOT1-USD", "Polkadot"),
                Pair("ADA-USD", "Cardano"),
                Pair("XRP-USD", "XRP"))
        
        coins.forEach {
            _crypto.add(SymbolEntity(it.key, it.value, "Coin/CryptoCurrency"))
        }
    }
    override fun getCount(): Int = _results.size
    override fun getItem(index: Int): SymbolEntity = _results[index]

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        }

        val tv = (view!!.findViewById<View>(android.R.id.text1) as TextView)
        if (position < _results.size) {
            val symbol = _results[position]
            tv.text = "${symbol.symbol} - ${symbol.name}"
        }
        else
            tv.text = "" // TODO look into this more, may happen if array is changed before old view is updated

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            @SuppressLint("DefaultLocale")
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

                    // TODO temp until in database, add crypto currency manually
                    if (constraint.toString().toUpperCase().startsWith("COI") || constraint.toString().toUpperCase().startsWith("CRYP"))
                        _results.addAll(_crypto)
                    else if (constraint.length >= 3) {
                        val search = constraint.toString()
                        val coins = _crypto.filter {
                            it.symbol.startsWith(search, true) || it.name.startsWith(search, true)
                        }

                        _results.addAll(coins)
                    }
                }
                else {
                    _prefs.getSymbolHistory()
                            .map { SymbolEntity(it.symbol, it.name ?: "", "") }
                            .sortedBy { it.symbol }
                            .forEach {
                                _results.add(it)
                            }
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