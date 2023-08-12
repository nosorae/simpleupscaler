package com.yessorae.simpleupscaler.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.ui.theme.Dimen

@Composable
fun SingleImage(modifier: Modifier = Modifier, bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = stringResource(id = R.string.cd_select_image),
        modifier = modifier
            .background(color = Color.Transparent, shape = RoundedCornerShape(Dimen.image_radius))
            .clip(RoundedCornerShape(Dimen.image_radius)),
        alignment = Alignment.Center,

        )
}
