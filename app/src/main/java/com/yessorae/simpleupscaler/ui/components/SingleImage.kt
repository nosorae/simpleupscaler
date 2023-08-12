package com.yessorae.simpleupscaler.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import com.yessorae.simpleupscaler.R

@Composable
fun SingleImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = stringResource(id = R.string.cd_select_image),
        modifier = Modifier.fillMaxWidth()
    )
}