package com.yessorae.simpleupscaler.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.ui.components.ActionButtonWithAd
import com.yessorae.simpleupscaler.ui.components.EmptyImage
import com.yessorae.simpleupscaler.ui.components.ImageComparer
import com.yessorae.simpleupscaler.ui.components.OutlinedActionButton
import com.yessorae.simpleupscaler.ui.components.SingleImage
import com.yessorae.simpleupscaler.ui.components.UpscaleTopAppBar
import com.yessorae.simpleupscaler.ui.model.UpscaleScreenState
import com.yessorae.simpleupscaler.ui.theme.Dimen
import com.yessorae.simpleupscaler.ui.util.IntentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: UpscaleViewModel = viewModel()) {
    val state by viewModel.screenState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            UpscaleTopAppBar(
                onClickHelp = {
                    // TODO
                }
            )
        },
        bottomBar = {
            AdmobBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { innerPadding ->
        BodyScreen(
            modifier = Modifier.padding(innerPadding),
            state = state,
            onSelectImage = { before ->
                viewModel.onSelectImage(before)
            },
            onClickUpscaleImage = { before, hasFace ->
                viewModel.upscaleImage(
                    before = before,
                    hasFace = hasFace
                )
            }
        )
    }
}

@Composable
fun BodyScreen(
    modifier: Modifier = Modifier,
    state: UpscaleScreenState,
    onSelectImage: (before: Bitmap) -> Unit,
    onClickUpscaleImage: (after: Bitmap, hasFace: Boolean) -> Unit
) {
    val context = LocalContext.current
    val screenCoroutineScope = rememberCoroutineScope()

    val takePhotoFromAlbumLauncher = // 갤러리에서 사진 가져오기
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    uriToBitmap(context = context, selectedFileUri = uri)?.let { bitmap ->
                        onSelectImage(bitmap)
                    } ?: run {
                        // TODO record exception
                    }
                } ?: run {
                    // TODO record exception
                }
            }
        }

    Column(modifier = modifier.padding(horizontal = Dimen.space_16)) {
        when (state) {
            is UpscaleScreenState.Start -> {
                StartScreen(
                    onClick = {
                        takePhotoFromAlbumLauncher.launch(IntentUtil.createGalleryIntent())
                    }
                )
            }

            is UpscaleScreenState.BeforeEnhance -> {
                BeforeEnhanceScreen(
                    selectedImage = state.image,
                    onClickReselectImage = {
                        takePhotoFromAlbumLauncher.launch(IntentUtil.createGalleryIntent())
                    },
                    onClickUpscaleImage = { before, hasFace ->
                        onClickUpscaleImage(before, hasFace)
                    }
                )
            }

            is UpscaleScreenState.Loading -> {
                LoadingScreen()
            }

            is UpscaleScreenState.AfterEnhance -> {
                AfterEnhanceScreen(
                    before = state.before,
                    after = state.after,
                    onClickReselectImage = {
                        takePhotoFromAlbumLauncher.launch(IntentUtil.createGalleryIntent())
                    },
                    onClickSave = { after ->
                        screenCoroutineScope.launch(Dispatchers.IO) {
                            saveImageUrlToGallery(context = context, bitmap = after)
                        }
                    }
                )
            }

            is UpscaleScreenState.Error -> {
                ErrorScreen()
            }

            else -> {
                ErrorScreen()
            }
        }
    }
}

@Composable
fun ColumnScope.StartScreen(
    onClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(Dimen.space_16))

    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        EmptyImage(modifier = Modifier.fillMaxWidth())
    }
    Spacer(modifier = Modifier.height(Dimen.space_16))
    OutlinedActionButton(
        text = stringResource(id = R.string.common_select_image),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Dimen.space_16))
}

@Composable
fun ColumnScope.BeforeEnhanceScreen(
    selectedImage: Bitmap,
    onClickReselectImage: () -> Unit,
    onClickUpscaleImage: (before: Bitmap, hasFace: Boolean) -> Unit
) {
    var hasFace by remember {
        mutableStateOf(true)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(
            checked = hasFace,
            onCheckedChange = {
                hasFace = it
            }
        )
        Spacer(modifier = Modifier.width(Dimen.space_4))
        Text(
            text = stringResource(
                id = if (hasFace) {
                    R.string.main_has_face
                } else {
                    R.string.main_do_not_has_face
                }
            ),
            style = MaterialTheme.typography.labelMedium,
            color = if (hasFace) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    }

    SingleImage(
        bitmap = selectedImage,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    )
    Spacer(modifier = Modifier.height(Dimen.space_16))
    Row {
        OutlinedActionButton(
            text = stringResource(id = R.string.common_reselect_image),
            onClick = onClickReselectImage,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Dimen.space_8))
        ActionButtonWithAd(
            text = stringResource(id = R.string.common_enhance_image),
            onClick = { onClickUpscaleImage(selectedImage, hasFace) },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(Dimen.space_16))
}

@Composable
fun ColumnScope.LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ColumnScope.AfterEnhanceScreen(
    before: Bitmap,
    after: Bitmap,
    onClickReselectImage: () -> Unit,
    onClickSave: (after: Bitmap) -> Unit
) {
    Spacer(modifier = Modifier.height(Dimen.space_16))

    ImageComparer(
        before = before,
        after = after
    )

    Spacer(modifier = Modifier.height(Dimen.space_16))

    Row {
        OutlinedActionButton(
            text = stringResource(id = R.string.common_reselect_image),
            onClick = onClickReselectImage,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Dimen.space_8))
        ActionButtonWithAd(
            text = stringResource(id = R.string.common_save),
            onClick = { onClickSave(after) },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(Dimen.space_16))
}

@Composable
fun ColumnScope.ErrorScreen() {
    // TODO:: #2
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    // TODO:: #3
}

private fun uriToBitmap(context: Context, selectedFileUri: Uri): Bitmap? {
    return try {
        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(selectedFileUri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        var image = BitmapFactory.decodeFileDescriptor(fileDescriptor)

        // 이미지의 회전 정보를 읽어옵니다.
        val inputStream = context.contentResolver.openInputStream(selectedFileUri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        // 필요한 경우 이미지를 회전시킵니다.
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)

        parcelFileDescriptor?.close()
        image
    } catch (e: IOException) {
        // TODO record exception
        e.printStackTrace()
        null
    }
}

private suspend fun saveImageUrlToGallery(context: Context, bitmap: Bitmap) {
    val dateText = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val name = "simple_upscaler_$dateText.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri =
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        }
    }
}
