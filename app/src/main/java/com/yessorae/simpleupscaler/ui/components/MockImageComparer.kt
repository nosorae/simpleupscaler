package com.yessorae.simpleupscaler.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.ui.theme.Dimen

@Composable
fun ColumnScope.MockImageComparer(
    modifier: Modifier = Modifier,
    before: Bitmap?,
    after: Bitmap?
) {
    val density = LocalDensity.current

    val screenWidth = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }

    var offset by remember { mutableStateOf(0f) }

    var indicatorSize by remember {
        mutableStateOf(0f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        after?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = stringResource(id = R.string.cd_after_image),
                modifier = Modifier
                    .background(color = Color.Transparent)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimen.image_radius)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }

        before?.let { image ->
            Image(
                bitmap = before.asImageBitmap(),
                contentDescription = stringResource(id = R.string.cd_before_image),
                modifier = Modifier
                    .background(color = Color.Transparent)
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val path = Path()
                        path.addRect(
                            Rect(
                                topLeft = Offset(0f, 0f),
                                bottomRight = Offset(size.width, size.height)
                            )
                        )
                        onDrawWithContent {
                            clipPath(path) {
                                this@onDrawWithContent.drawContent()
                            }
                            drawRect(
                                topLeft = Offset(offset + (indicatorSize / 2), 0f),
                                size = androidx.compose.ui.geometry.Size(screenWidth, size.height),
                                color = Color.Transparent,
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                    .clip(RoundedCornerShape(Dimen.image_radius)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
                .offset(x = with(density) { offset.toDp() }, y = 0.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        offset += dragAmount
                        change.consume()
                    }
                }
                .graphicsLayer {
                    indicatorSize = size.width
                },
            contentAlignment = Alignment.Center
        ) {
            val lineColor = Color.White.copy(alpha = 0.8f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = lineColor)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Text(
//                    text = stringResource(id = R.string.main_before),
//                    style = MaterialTheme.typography.labelSmall,
//                    color = lineColor
//                )
                Icon(
                    imageVector = Icons.Outlined.ArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.small_icon_size),
                    tint = lineColor
                )
                Spacer(modifier = Modifier.width(Dimen.space_4))
                Icon(
                    imageVector = Icons.Outlined.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.small_icon_size),
                    tint = lineColor
                )
//                Text(
//                    text = stringResource(id = R.string.main_after),
//                    style = MaterialTheme.typography.labelSmall,
//                    color = lineColor
//                )
            }
        }
    }
}
