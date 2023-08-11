package com.yessorae.simpleupscaler.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yessorae.simpleupscaler.common.Logger.printLog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

@Composable
fun UpscaleScreen(viewModel: UpscaleViewModel = viewModel()) {
    var originalImage by remember { mutableStateOf<Bitmap?>(null) }
    val upscaledImage by viewModel.resultImageUrl.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val takePhotoFromAlbumLauncher = // 갤러리에서 사진 가져오기
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    originalImage = uriToBitmap(context = context, selectedFileUri = uri)
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
            val intent = Intent(
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
            takePhotoFromAlbumLauncher.launch(intent)
            printLog("takePhotoFromAlbumLauncher.launch(intent)")
        }) {
            Text("Select Image")
        }

        originalImage?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Original Image", modifier = Modifier.fillMaxWidth())
        }

        Button(onClick = {
            isLoading = true
            originalImage?.let { image ->
                val imagePart = image.toMultiPartBody()
                val type = "face".toRequestBody("text/plain".toMediaTypeOrNull())
                val sync = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                viewModel.upscaleImage(
                    imageFile = imagePart,
                    type = type,
                    sync = sync
                )

//                        upscaledImage = result
//                        isLoading = false
            }
        }) {
            Text("Upscale Image")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        upscaledImage?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
        }
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

fun uriToBitmap(context: Context, selectedFileUri: Uri): Bitmap? {
    return try {
        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(selectedFileUri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        image
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
