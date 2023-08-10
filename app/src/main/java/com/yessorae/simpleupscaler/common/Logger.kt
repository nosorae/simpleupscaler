package com.yessorae.simpleupscaler.common

import android.util.Log

object Logger {
    fun printLog(message: String) {
        Log.d("SR-N", message)
    }
    fun printError(message: String) {
        Log.e("SR-N", message)
    }
}