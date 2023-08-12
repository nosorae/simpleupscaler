package com.yessorae.simpleupscaler.ui.model

import android.graphics.Bitmap

sealed class UpscaleScreenState {
    object Start : UpscaleScreenState()

    data class BeforeEnhance(
        val image: Bitmap
    ) : UpscaleScreenState()

    object Loading : UpscaleScreenState()

    data class AfterEnhance(
        val before: Bitmap,
        val after: Bitmap
    ) : UpscaleScreenState()

    data class Error(
        val message: String
    ) : UpscaleScreenState()
}
