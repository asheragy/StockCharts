package org.cerion.stockcharts.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stocks.core.model.Symbol;
import org.cerion.stocks.core.web.CachedDataAPI;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class SymbolAutoCompleteTextView extends AutoCompleteTextView {

    public SymbolAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public SymbolAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setThreshold(1); // Some symbols are only 1 character
        SymbolAutoCompleteAdapter adapter = new SymbolAutoCompleteAdapter(getContext(), android.R.layout.simple_dropdown_item_1line);
        setAdapter(adapter);
    }

    private class SymbolAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        private ArrayList<Symbol> mResults;
        private List<Symbol> mDatabaseList;
        private SymbolRepository repo;
        private CachedDataAPI api;
        private Context mContext;

        SymbolAutoCompleteAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);

            mContext = context;
            mResults = new ArrayList<>();
            repo = new SymbolRepository(context);
            api = Injection.getAPI(context);

            mDatabaseList = new SymbolRepository(context).getAll();
        }

        @Override
        public int getCount() {
            return mResults.size();
        }

        @Override
        public String getItem(int index) {
            return mResults.get(index).getSymbol();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }

            Symbol symbol = mResults.get(position);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(symbol.getSymbol() + " - " + symbol.getName());
            return convertView;
        }

        @Override
        @NonNull
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    // If not focused this was probably set in code and shouldn't show the filter
                    if(constraint != null && isFocused()) {
                        mResults.clear();

                        Symbol symbol = api.getSymbol(constraint + "");
                        if (symbol != null && symbol.isValid())
                            mResults.add(symbol);

                        for (Symbol s : mDatabaseList) {
                            String lookup = constraint + "";
                            if(s.getSymbol().startsWith(lookup) && s.getSymbol().length() > lookup.length()) {
                                mResults.add(s);
                            }
                        }

                        filterResults.values = mResults;
                        filterResults.count = mResults.size();
                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence contraint, FilterResults results) {
                    if(results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }
}
