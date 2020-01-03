package org.cerion.stockcharts

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.cerion.stockcharts.common.GenericAsyncTask
import org.cerion.stockcharts.common.GenericAsyncTask.TaskHandler
import org.cerion.stockcharts.ui.positions.PositionsFragment
import org.cerion.stockcharts.ui.symbols.SymbolsFragment
import org.cerion.stockcharts.ui.watchlist.WatchListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val adapter = ViewPagerAdapter(this)
        val viewPager = findViewById<ViewPager2>(R.id.container)
        viewPager.adapter = adapter

        val tabLayout = findViewById<TabLayout>(R.id.sliding_tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.import_sp500) { //onImportSP500();
            return true
        } else if (id == R.id.clear_cache) {
            onClearCache()
            return true
        } else if (id == R.id.log_database) {
            onLogDatabase()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onLogDatabase() { // TODO add database logging to the database class
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show()
    }

    private fun onClearCache() {
        val task = GenericAsyncTask(object : TaskHandler {
            override fun run() { //Injection.getAPI(MainActivity.this).clearCache();
            }

            override fun onFinish() {
                Toast.makeText(this@MainActivity, "Cache cleared", Toast.LENGTH_SHORT).show()
            }
        })
        task.execute()
    }

    private inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> SymbolsFragment()
                1 -> PositionsFragment()
                2 -> WatchListFragment()
                else -> throw NotImplementedError()
            }
        }

        fun getTitle(position: Int): String {
            return when (position) {
                0 -> "Symbols"
                1 -> "Positions"
                2 -> "Watch List"
                else -> throw NotImplementedError()
            }
        }
    }
}
