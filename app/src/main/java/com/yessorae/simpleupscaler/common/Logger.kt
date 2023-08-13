package com.yessorae.simpleupscaler.common

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.BuildConfig
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object Logger {
    fun printLog(message: String) {
        Log.d("SR-N", message)
    }
    fun printError(message: String) {
        Log.e("SR-N", message)
    }
    fun recordException(e: Throwable) {
        Firebase.crashlytics.recordException(e)
    }

    fun logAnalyticsEvent(
        event: String,
        vararg params: Pair<String, Any?>
    ) {
        if (BuildConfig.DEBUG) return

        Bundle().apply {
            params.forEach { pair ->
                when (val value = pair.second) {
                    is Int -> {
                        putInt(pair.first, value)
                    }

                    is Boolean -> {
                        putBoolean(pair.first, value)
                    }

                    else -> {
                        (value as? String)?.let {
                            putString(pair.first, value)
                        } ?: run {
                            putString(
                                pair.first,
                                value.toString()
                            )
                        }
                    }
                }
            }
        }.also { bundle ->
            Firebase.analytics.logEvent(event, bundle)
        }
    }
}
