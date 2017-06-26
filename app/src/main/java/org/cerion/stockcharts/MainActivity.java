package org.cerion.stockcharts;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.positions.PositionListFragment;
import org.cerion.stockcharts.repository.DividendRepository;
import org.cerion.stockcharts.repository.MasterRepository;
import org.cerion.stockcharts.repository.PriceListRepository;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stocklist.model.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

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
        mViewPager = (ViewPager) findViewById(R.id.container);
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
            onImportSP500();
            return true;
        } else if(id == R.id.clear_cache) {
            onClearCache();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
                SymbolListFragment frag = (SymbolListFragment)mSectionsPagerAdapter.getActiveFragment(mViewPager, 0);
                frag.refresh();
                Toast.makeText(MainActivity.this,"Finished import",Toast.LENGTH_LONG).show();
            }
        });

        task.execute();
        //List<String> symbols = Symbols.getSP500List();
    }

    private void onClearCache() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {

            private long spaceSaved = 0;
            @Override
            public void run() {
                spaceSaved = new MasterRepository(MainActivity.this).clearCache();
            }

            @Override
            public void onFinish() {
                long kb = spaceSaved / 1024;
                Toast.makeText(MainActivity.this,String.format("Removed %sKB", kb),Toast.LENGTH_SHORT).show();
            }
        });

        task.execute();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter { // TODO use v13 support library with this so fragments don't have to be v4

        private FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {

            if(position == 0)
                return new SymbolListFragment();
            else if(position == 1)
                return new PositionListFragment();

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Symbols";
                case 1:
                    return "Positions";
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


}
