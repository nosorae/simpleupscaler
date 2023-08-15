package com.yessorae.simpleupscaler.common

sealed class UpscaleExceptions(message: String) : Throwable(message = message) {
    data class CustomException(override val message: String) : UpscaleExceptions(message)
    data class AdException(override val message: String) : UpscaleExceptions(message)
}
