package org.cerion.stockcharts.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cerion.stockcharts.R
import org.cerion.stockcharts.repository.PriceListSQLRepository
import org.cerion.stockcharts.ui.crypto.CryptoFragment
import org.cerion.stockcharts.ui.symbols.SymbolCategory
import org.cerion.stockcharts.ui.symbols.SymbolsFragment
import org.cerion.stockcharts.ui.watchlist.WatchListFragment

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val adapter = ViewPagerAdapter(this)
        val viewPager = view.findViewById<ViewPager2>(R.id.container)
        viewPager.adapter = adapter

        val tabLayout = view.findViewById<TabLayout>(R.id.sliding_tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        return view.rootView
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    @Deprecated("Deprecated in Java")
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
        Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT).show()
    }

    private fun onClearCache() {
        val repo = PriceListSQLRepository(requireContext())
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                repo.clearCache()
            }

            Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> CryptoFragment()
                1 -> SymbolsFragment.newInstance(SymbolCategory.FUND)
                2 -> SymbolsFragment.newInstance(SymbolCategory.STOCK)
                else -> throw NotImplementedError()
            }
        }

        fun getTitle(position: Int): String {
            return when (position) {
                0 -> "Crypto"
                1 -> "ETF"
                2 -> "Stocks"
                else -> throw NotImplementedError()
            }
        }
    }
}