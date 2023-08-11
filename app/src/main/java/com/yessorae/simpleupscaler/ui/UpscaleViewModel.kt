package com.yessorae.simpleupscaler.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yessorae.simpleupscaler.data.repository.UpscaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class UpscaleViewModel @Inject constructor(
    private val upscaleRepository: UpscaleRepository
) : ViewModel() {
    private val _resultImageUrl = MutableStateFlow<String?>(null)
    val resultImageUrl = _resultImageUrl.asStateFlow()
    fun upscaleImage(
        imageFile: MultipartBody.Part,
        type: RequestBody,
        sync: RequestBody
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val response = upscaleRepository.upscaleImage(
                imageFile = imageFile,
                type = type,
                sync = sync
            )
            _resultImageUrl.value = response?.image
        } catch (e: Exception) {
            Log.e("SR-N", "Exception: $e")
        }
    }
}
