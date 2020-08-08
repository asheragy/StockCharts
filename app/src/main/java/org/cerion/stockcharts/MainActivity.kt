package org.cerion.stockcharts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO base on device or setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

val Fragment.appCompatActivity: AppCompatActivity?
    get() = if(requireActivity() is AppCompatActivity) requireActivity() as AppCompatActivity else null