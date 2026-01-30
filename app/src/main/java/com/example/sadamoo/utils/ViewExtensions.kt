package com.example.sadamoo.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applyStatusBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.setPadding(
            view.paddingLeft,
            statusBarHeight,
            view.paddingRight,
            view.paddingBottom
        )
        insets
    }
}
