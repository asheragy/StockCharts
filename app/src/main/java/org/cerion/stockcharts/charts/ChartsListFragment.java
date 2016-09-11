package org.cerion.stockcharts.charts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.cerion.stockcharts.R;

import java.util.ArrayList;
import java.util.List;

public class ChartsListFragment extends ListFragment {

    private ArrayAdapter<String> mAdapter;
    private List<String> mLists = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.charts_list_fragment, container, false);

        mLists.add("Add Indicator");
        mLists.add("Add Overlay");
        mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mLists);
        setListAdapter(mAdapter);

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(),ChartEditActivity.class);
        startActivity(intent);
    }

    /*
    private static final String TAG = TemplateListActivity.class.getSimpleName();

    private TemplateListAdapter mAdapter;
    private ListView mListView;

    private void refresh()
    {
        if(mAdapter == null) {
            mAdapter = new TemplateListAdapter(TemplateListActivity.this);
            mListView.setAdapter(mAdapter);
        }

        mAdapter.refresh(null); //TODO set data
    }

    private class DataType //TODO placeholder for real class
    {
    }


    private class TemplateListAdapter extends ArrayAdapter<DataType> {
        private static final int RESOURCE_ID = 0; //TODO Adapter row layout ID

        public TemplateListAdapter(Context context) {
            super(context, RESOURCE_ID);

        }
        public void refresh(List<DataType> data) {
            clear();
            addAll(data);
            notifyDataSetChanged();
        }

        private class ViewHolder {
            //TODO add views
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DataType item = getItem(position);
            ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(RESOURCE_ID, parent, false);

                viewHolder = new ViewHolder();
                //TODO set views
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //TODO set data

            return convertView;
        }
    }
    */

}
