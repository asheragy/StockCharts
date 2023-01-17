package org.cerion.stockcharts.common

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import org.cerion.stockcharts.BuildConfig

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun Context.isDarkTheme(): Boolean = isEmulator() || (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES)

fun isEmulator(): Boolean = BuildConfig.DEBUG && Build.FINGERPRINT.contains("generic")


