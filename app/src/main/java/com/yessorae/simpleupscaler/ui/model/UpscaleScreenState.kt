package com.yessorae.simpleupscaler.ui.model

import android.graphics.Bitmap

sealed class UpscaleScreenState {
    object Start : UpscaleScreenState()

    data class BeforeEnhance(
        val image: Bitmap
    ) : UpscaleScreenState()

    object Loading : UpscaleScreenState()

    data class AfterEnhance(
        val beforeImageBitmap: Bitmap,
        val afterImageUrl: Bitmap
    ) : UpscaleScreenState()

    object Error : UpscaleScreenState()
}
