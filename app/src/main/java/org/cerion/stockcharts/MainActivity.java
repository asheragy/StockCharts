package org.cerion.stockcharts;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.positions.PositionListFragment;
import org.cerion.stockcharts.ui.symbols.SymbolsFragment;
import org.cerion.stockcharts.watchlist.WatchListFragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(3);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.import_sp500) {
            //onImportSP500();
            onViewPortfolio();
            return true;
        }
        else if(id == R.id.clear_cache) {
            onClearCache();
            return true;
        }
        else if(id == R.id.log_database) {
            onLogDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLogDatabase() {
        // TODO add database logging to the database class
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
    }

    private void onViewPortfolio() {
        startActivity(PortfolioValueActivity.getIntent(this));
    }


    private void onClearCache() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                Injection.getAPI(MainActivity.this).clearCache();
            }

            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this,"Cache cleared",Toast.LENGTH_SHORT).show();
            }
        });

        task.execute();
    }

    private class SectionsPagerAdapter extends PagerAdapter {
        private static final int COUNT = 3;
        private final FragmentManager mFragmentManager;
        private Fragment[] mFragments;
        private FragmentTransaction mCurTransaction;

        private SectionsPagerAdapter(FragmentManager fragmentManager) {
            mFragmentManager = fragmentManager;
            mFragments = new Fragment[COUNT];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = mFragments[position];

            if (fragment == null)
                fragment = getFragment(position);

            if (fragment == null) {
                fragment = getItem(position);
                if (mCurTransaction == null) {
                    mCurTransaction = mFragmentManager.beginTransaction();
                }

                mCurTransaction.add(container.getId(), fragment, getTag(position));
            }

            return fragment;
        }

        /*
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            mCurTransaction.detach(mFragments.get(position));
            mFragments.remove(position);
        }
        */

        @Override
        public boolean isViewFromObject(View view, Object fragment) {
            return ((Fragment) fragment).getView() == view;
        }

        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFragments[0] = new SymbolsFragment();
                    break;
                case 1:
                    mFragments[1] = new PositionListFragment();
                    break;
                case 2:
                    mFragments[2] = new WatchListFragment();
                    break;
            }

            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Symbols";
                case 1:
                    return "Positions";
                case 2:
                    return "Watch List";
            }

            return null;
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        public Fragment getFragment(int position) {
            String tag = getTag(position);
            return mFragmentManager.findFragmentByTag(tag);
        }

        private String getTag(int position) {
            return "fragment:" + position;
        }
    }
}
