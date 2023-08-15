package com.yessorae.simpleupscaler.ui.model

import android.graphics.Bitmap

sealed class UpscaleScreenState {
    object Start : UpscaleScreenState()

    data class BeforeEnhance(
        val image: Bitmap,
        val retry: Boolean = false
    ) : UpscaleScreenState()

    data class Loading(
        val progress: Int
    ) : UpscaleScreenState()

    data class AfterEnhance(
        val before: Bitmap,
        val after: String
    ) : UpscaleScreenState()

    data class Error(
        val message: String
    ) : UpscaleScreenState()
}
