package org.cerion.stockcharts;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.positions.PositionListFragment;
import org.cerion.stockcharts.ui.symbols.SymbolsFragment;
import org.cerion.stockcharts.watchlist.WatchListFragment;


public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        /*
        mViewPager.setPageTransformer(true, new PageTransformer() {

            @Override
            public void transformPage(View page, float position) {
                Log.d("main","position = " + position);
            }
        });
        */
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);


        //StockDB db = StockDB.getInstance(this);
        //db.symbols.deleteAll();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.import_sp500) {
            //onImportSP500();
            onViewPortfolio();
            return true;
        } else if(id == R.id.clear_cache) {
            onClearCache();
            return true;
        } else if(id == R.id.log_database) {
            onLogDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLogDatabase() {
        //new MasterRepository(this).log();
        // TODO add database logging to the database class
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
    }

    private void onViewPortfolio() {
        startActivity(PortfolioValueActivity.getIntent(this));
    }

    /*
    private void onImportSP500()
    {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                //List<String> symbols = Symbols.getSP500List();
                //DJI http://www.nasdaq.com/quotes/djia-stocks.aspx

                //List<String> symbols = new ArrayList<>( Arrays.asList("AAPL", "", "", "", "") );
                List<Symbol> symbols = new ArrayList<>(Arrays.asList(
                        new Symbol("AAPL", "Apple Inc.", ""),
                        new Symbol("GOOGL", "Google", ""),
                        new Symbol("MSFT", "Microsoft", ""),
                        new Symbol("^GSPC", "S&P 500", ""),
                        new Symbol("XLE", "Energy Select Sector SPDR ETF", "")
                ));

                SymbolRepository repo = new SymbolRepository(MainActivity.this);
                for(Symbol s : symbols) {
                    repo.add(s);
                }

                repo.log();
            }

            @Override
            public void onFinish() {
                SymbolsFragment frag = (SymbolsFragment)mSectionsPagerAdapter.getFragment(0);
                frag.refresh();
                Toast.makeText(MainActivity.this,"Finished import",Toast.LENGTH_LONG).show();
            }
        });

        task.execute();
        //List<String> symbols = Symbols.getSP500List();
    }
    */

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


    /*
    public class SectionsPagerAdapter extends FragmentPagerAdapter { // TODO use v13 support library with this so fragments don't have to be v4

        private FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {

            if(position == 0)
                return new SymbolsFragment();
            else if(position == 1)
                return new PositionListFragment();
            else if (position == 2)
                return new WatchListFragment();

            return null;
        }

        @Override
        public int getCount() {
            return 3;
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

        public Fragment getActiveFragment(ViewPager container, int position) {
            String name = makeFragmentName(container.getId(), position);
            return  mFragmentManager.findFragmentByTag(name);
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }
    }
    */

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
            switch(position) {
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
            return  mFragmentManager.findFragmentByTag(tag);
        }

        private String getTag(int position) {
            return "fragment:" + position;
        }
    }


}
