package com.example.example

import android.view.View

internal infix fun View.onClick(function: () -> Unit) {
    setOnClickListener { function() }
}