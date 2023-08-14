package com.yessorae.simpleupscaler.ui.util

import com.yessorae.simpleupscaler.common.Logger

fun String.safeFormat(vararg params: String): String {
    return try {
        this.format(params)
    } catch (e: Exception) {
        Logger.recordException(e)
        ""
    }
}