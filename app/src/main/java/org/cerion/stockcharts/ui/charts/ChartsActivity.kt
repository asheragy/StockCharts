package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.cerion.stockcharts.R

class ChartsActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context, symbol: String): Intent {
            val intent = Intent(context, ChartsActivity::class.java)
            intent.putExtra("symbol", symbol)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)
    }
}
