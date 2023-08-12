package com.yessorae.simpleupscaler.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yessorae.simpleupscaler.data.repository.UpscaleRepository
import com.yessorae.simpleupscaler.ui.model.UpscaleScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class UpscaleViewModel @Inject constructor(
    private val upscaleRepository: UpscaleRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow<UpscaleScreenState>(UpscaleScreenState.Start)
    val screenState: StateFlow<UpscaleScreenState> = _screenState.asStateFlow()

    fun upscaleImage(
        before: Bitmap,
        hasFace: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        val imageFile = before.toMultiPartBody()
        val type = if (hasFace) {
            "face".toRequestBody("text/plain".toMediaTypeOrNull()) // face: Face Enhancement
        } else {
            "clean".toRequestBody("text/plain".toMediaTypeOrNull()) // clean: Whole Photo Enhancement
        }

        val sync = "1".toRequestBody("text/plain".toMediaTypeOrNull()) // 1: Synchronize
        val returnType =
            "2".toRequestBody("text/plain".toMediaTypeOrNull()) // 2: Return the image as a base64 string

        try {
            _screenState.value = UpscaleScreenState.Loading

            val response = upscaleRepository.upscaleImage(
                imageFile = imageFile,
                type = type,
                sync = sync,
                returnType = returnType
            )
            val base64String = response?.image
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val afterBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            _screenState.value = UpscaleScreenState.AfterEnhance(
                before = before,
                after = afterBitmap
            )
        } catch (e: Exception) {
            // TODO record exception
            Log.e("SR-N", "Exception: $e")
            _screenState.value = UpscaleScreenState.Error(e.toString())
        }
    }

    fun onError(e: Throwable) {
        // TODO record exception
        Log.e("SR-N", "Exception: $e")
        _screenState.value = UpscaleScreenState.Error(e.toString())
    }

    fun onSelectImage(bitmap: Bitmap) {
        _screenState.value = UpscaleScreenState.BeforeEnhance(bitmap)
    }

    private fun Bitmap.toMultiPartBody(): MultipartBody.Part {
        val byteArrayOutputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val requestFile = byteArray.toRequestBody("image/png".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image_file", "image.png", requestFile)
    }
}
