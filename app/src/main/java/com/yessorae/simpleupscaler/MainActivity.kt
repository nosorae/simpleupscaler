package com.yessorae.simpleupscaler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.yessorae.simpleupscaler.ui.theme.SimpleupscalerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleupscalerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screen()
                }
            }
        }
    }

    @Composable
    fun Screen() {
        var originalImage by remember { mutableStateOf<Bitmap?>(null) }
        var upscaledImage by remember { mutableStateOf<String?>(null) }
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

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
                    coroutineScope.launch(Dispatchers.IO) {
                        val result =
                            upscaleImageWithDeepAi(image, "e8847bb0-aed7-4ddf-aad5-68d8722d102b")
                        printLog("upscaleImageWithDeepAi result: $result")
                        withContext(Dispatchers.Main) {
                            upscaledImage = result
                            isLoading = false
                        }
                    }
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

    suspend fun upscaleImageWithDeepAi(image: Bitmap, apiKey: String): String? {
        printLog("upscaleImageWithDeepAi")
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val requestBody = RequestBody.create(
            "image/png".toMediaTypeOrNull(),
            byteArrayOutputStream.toByteArray()
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deepai.org/")
            .build()

        val service = retrofit.create(DeepAiService::class.java)
        val call = service.upscaleImage(requestBody, apiKey)

        val response = call.execute()

//        printLog("response.isSuccessful: ${response.isSuccessful}")
//        printError("response.errorBody: ${response.errorBody()?.string()}")
        return if (response.isSuccessful) {
            val jsonResponse = JSONObject(response.body()?.string())
            jsonResponse.getString("output_url")
        } else {
            null
        }
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

    @Suppress("DEPRECATION", "NewApi")
    private fun Uri.parseBitmap(context: Context): Bitmap {
        return when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 28
            true -> {
                val source = ImageDecoder.createSource(context.contentResolver, this)
                ImageDecoder.decodeBitmap(source)
            }

            else -> {
                MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            }
        }
    }

    fun printLog(message: String) {
        Log.d("SR-N", message)
    }
    fun printError(message: String) {
        Log.e("SR-N", message)
    }
}
