package org.cerion.stockcharts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import org.cerion.stockcharts.common.isEmulator


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isEmulator())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

val Fragment.appCompatActivity: AppCompatActivity?
    get() = if(requireActivity() is AppCompatActivity) requireActivity() as AppCompatActivity else null