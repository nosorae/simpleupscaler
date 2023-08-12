package com.yessorae.simpleupscaler.ui.util

import android.content.Intent
import android.provider.MediaStore

object IntentUtil {
    fun createGalleryIntent(): Intent {
        return Intent(
            Intent.ACTION_GET_CONTENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("image/jpeg", "image/png", "image/bmp", "image/webp")
            )
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
    }
}
