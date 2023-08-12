package com.yessorae.simpleupscaler.ui.model

import android.graphics.Bitmap

sealed class UpscaleScreenState {
    object Start : UpscaleScreenState()

    data class BeforeEnhance(
        val image: Bitmap
    ) : UpscaleScreenState()

    object Loading : UpscaleScreenState()

    data class AfterEnhance(
        val image: Bitmap,
        val imageUrl: String
    ) : UpscaleScreenState()

    data class End(
        val image: Bitmap,
        val imageUrl: String
    ) : UpscaleScreenState()

    object Error : UpscaleScreenState()
}
