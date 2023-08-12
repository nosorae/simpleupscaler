package com.yessorae.simpleupscaler.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.common.Logger.printError
import com.yessorae.simpleupscaler.common.Logger.printLog
import com.yessorae.simpleupscaler.ui.components.ActionButtonWithAd
import com.yessorae.simpleupscaler.ui.components.EmptyImage
import com.yessorae.simpleupscaler.ui.components.ImageComparer
import com.yessorae.simpleupscaler.ui.components.OutlinedActionButton
import com.yessorae.simpleupscaler.ui.components.SingleImage
import com.yessorae.simpleupscaler.ui.components.UpscaleTopAppBar
import com.yessorae.simpleupscaler.ui.model.UpscaleScreenState
import com.yessorae.simpleupscaler.ui.theme.Dimen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
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
            onSelectImage = {
                viewModel.onSelectImage(it)
            },
            onClickUpscaleImage = { bitmap ->
                val imagePart = bitmap.toMultiPartBody()
                viewModel.upscaleImage(
                    bitmap = bitmap,
                    imageFile = imagePart,
                )
            }
        )
    }
}

@Composable
fun BodyScreen(
    modifier: Modifier = Modifier,
    state: UpscaleScreenState,
    onSelectImage: (Bitmap) -> Unit,
    onClickUpscaleImage: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val screenCoroutineScope = rememberCoroutineScope()

    val takePhotoFromAlbumLauncher = // 갤러리에서 사진 가져오기
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    uriToBitmap2(context = context, selectedFileUri = uri)?.let { bitmap ->
                        onSelectImage(bitmap)
                    } ?: run {
                        // TODO record exception
                    }
                } ?: run {
                    // TODO record exception
                }
            } else if (result.resultCode != Activity.RESULT_CANCELED) {
                printLog("takePhotoFromAlbumLauncher result is not ok") // TODO event
            }
        }

    Column(modifier = Modifier.padding(Dimen.space_16)) {
        when (state) {
            is UpscaleScreenState.Start -> {
                StartScreen(
                    onClick = {
                        takePhotoFromAlbumLauncher.launch(createGalleryIntent())
                    }
                )
            }

            is UpscaleScreenState.BeforeEnhance -> {
                BeforeEnhanceScreen(
                    selectedImage = state.image,
                    onClickReselectImage = {
                        takePhotoFromAlbumLauncher.launch(createGalleryIntent())
                    },
                    onClickUpscaleImage = {
                        onClickUpscaleImage(it)
                    }
                )
            }

            is UpscaleScreenState.Loading -> {
                LoadingScreen()
            }

            is UpscaleScreenState.AfterEnhance -> {
                AfterEnhanceScreen(
                    beforeImageBitmap = state.beforeImageBitmap,
                    afterImageUrl = state.afterImageUrl,
                    onClickReselectImage = {
                        takePhotoFromAlbumLauncher.launch(createGalleryIntent())
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
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        EmptyImage(modifier = Modifier.fillMaxWidth())
    }
    Spacer(modifier = Modifier.height(Dimen.space_16))
    OutlinedActionButton(
        text = stringResource(id = R.string.common_select_image),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ColumnScope.BeforeEnhanceScreen(
    selectedImage: Bitmap,
    onClickReselectImage: () -> Unit,
    onClickUpscaleImage: (Bitmap) -> Unit
) {
    SingleImage(
        bitmap = selectedImage, modifier = Modifier
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
            onClick = { onClickUpscaleImage(selectedImage) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ColumnScope.LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ColumnScope.AfterEnhanceScreen(
    beforeImageBitmap: Bitmap,
    afterImageUrl: Bitmap,
    onClickReselectImage: () -> Unit,
    onClickSave: (after: Bitmap) -> Unit,
) {

    ImageComparer(
        beforeImageBitmap = beforeImageBitmap,
        afterImageUrl = afterImageUrl
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
            onClick = { onClickSave(afterImageUrl) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ColumnScope.ErrorScreen() {
    // TODO:: #2
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    // TODO:: #3
}

@Composable
fun UpscaleScreen(viewModel: UpscaleViewModel = viewModel()) {
    var originalImage by remember { mutableStateOf<Bitmap?>(null) }
//    val upscaledImage by viewModel.resultImageUrl.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val takePhotoFromAlbumLauncher = // 갤러리에서 사진 가져오기
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    originalImage = uriToBitmap2(context = context, selectedFileUri = uri)
                } ?: run {
                    printLog("uri is null")
                }
            } else if (result.resultCode != Activity.RESULT_CANCELED) {
                printLog("takePhotoFromAlbumLauncher result is not ok")
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = {
            takePhotoFromAlbumLauncher.launch(createGalleryIntent())
            printLog("takePhotoFromAlbumLauncher.launch(intent)")
        }) {
            Text("Select Image")
        }

        originalImage?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Original Image",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(onClick = {
            originalImage?.let { image ->
                val imagePart = image.toMultiPartBody()
                viewModel.upscaleImage(
                    bitmap = image,
                    imageFile = imagePart,
                )
            }
        }) {
            Text("Upscale Image")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

//        upscaledImage?.let {
//            AsyncImage(
//                model = it,
//                contentDescription = null,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
    }
}

fun getPathFromUri(context: Context, uri: Uri?): String? {
    var cursor: Cursor? = null
    try {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(uri!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    } finally {
        cursor?.close()
    }
}

fun Bitmap.toMultiPartBody(): MultipartBody.Part {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    val requestFile = byteArray.toRequestBody("image/jpg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image_file", "image.jpg", requestFile)
}

fun uriToBitmap2(context: Context, selectedFileUri: Uri): Bitmap? {
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

suspend fun saveImageUrlToGallery(context: Context, bitmap: Bitmap) {
    val dateText = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val name = "simple_upscaler_${dateText}.png"
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
