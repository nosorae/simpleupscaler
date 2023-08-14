package com.yessorae.simpleupscaler.common

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object Logger {
    fun debug(message: String) {
        Log.d("SR-N", message)
    }

    fun error(message: String) {
        Log.e("SR-N", message)
    }

    fun recordException(e: Throwable) {
        Firebase.crashlytics.recordException(e)
    }

    fun recordAdException(adError: LoadAdError) {
        Firebase.crashlytics.recordException(UpscaleExceptions.AdException(adError.toString()))
    }

    fun recordAdException(adError: AdError) {
        Firebase.crashlytics.recordException(UpscaleExceptions.AdException(adError.toString()))
    }

    fun recordCustomException(message: String) {
        Firebase.crashlytics.recordException(UpscaleExceptions.CustomException(message))
    }

    fun event(
        event: String,
        vararg params: Pair<String, Any?>
    ) {
        debugEvent(event, *params)
        try {
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
        } catch (e: Exception) {
            recordException(e)
        }
    }

    private fun debugEvent(
        event: String,
        vararg params: Pair<String, Any?>
    ) {
        debug(
            "$event\n${
                params.toList().joinToString(separator = "\n") { "\t${it.first} : ${it.second}" }
            }"
        )
    }
}
