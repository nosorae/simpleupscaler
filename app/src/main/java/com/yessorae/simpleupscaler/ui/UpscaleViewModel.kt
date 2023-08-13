package com.yessorae.simpleupscaler.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.LoadAdError
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.common.Logger
import com.yessorae.simpleupscaler.data.repository.UpscaleRepository
import com.yessorae.simpleupscaler.ui.model.ResString
import com.yessorae.simpleupscaler.ui.model.StringModel
import com.yessorae.simpleupscaler.ui.model.UpscaleScreenState
import com.yessorae.simpleupscaler.ui.util.HelpLink
import com.yessorae.simpleupscaler.ui.util.MockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UpscaleViewModel @Inject constructor(
    private val upscaleRepository: UpscaleRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow<UpscaleScreenState>(UpscaleScreenState.Start)
    val screenState: StateFlow<UpscaleScreenState> = _screenState.asStateFlow()

    private val _showUpscaleRewardAdEvent = MutableSharedFlow<UpscaleRequestParam>()
    val showUpscaleRewardAdEvent: SharedFlow<UpscaleRequestParam> =
        _showUpscaleRewardAdEvent.asSharedFlow()

    private val _showSaveInterstitialAdEvent = MutableSharedFlow<SaveRequestParam>()
    val showSaveInterstitialAdEvent: SharedFlow<SaveRequestParam> =
        _showSaveInterstitialAdEvent.asSharedFlow()

    private val _saveImageEvent = MutableSharedFlow<SaveRequestParam>()
    val saveImageEvent = _saveImageEvent.asSharedFlow()

    private val _redirectToWebBrowserEvent = MutableSharedFlow<String>()
    val redirectToWebBrowserEvent = _redirectToWebBrowserEvent.asSharedFlow()

    protected val _toast = MutableSharedFlow<StringModel>()
    val toast: SharedFlow<StringModel> = _toast.asSharedFlow()

    private fun upscaleImage(
        param: UpscaleRequestParam
    ) = viewModelScope.launch(Dispatchers.IO) {
        val imageFile = param.before.toMultiPartBody()
        val type = if (param.hasFace) {
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
            val base64String = MockData.MOCK_IMAGE_BASE64 // response?.image
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val afterBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            _screenState.value = UpscaleScreenState.AfterEnhance(
                before = param.before,
                after = afterBitmap
            )

            _toast.emit(ResString(R.string.toast_complete_enhance))
        } catch (e: Exception) {
            onErrorState(message = e.toString())
        }
    }

    private fun saveImage(param: SaveRequestParam) = viewModelScope.launch {
        _saveImageEvent.emit(param)
    }

    fun onErrorState(message: String) {
        // TODO record exception
        Log.e("SR-N", "Exception: $message")
        _screenState.value = UpscaleScreenState.Error(message)
    }

    fun onSelectImage(bitmap: Bitmap) {
        _screenState.value = UpscaleScreenState.BeforeEnhance(bitmap)
    }

    fun onClickRequestUpscale(param: UpscaleRequestParam) {
        showUpscaleAd(param)
    }

    fun onCompleteUpscaleRewardAdmob(param: UpscaleRequestParam) {
        upscaleImage(param = param)
    }

    fun onClickRequestSave(param: SaveRequestParam) = viewModelScope.launch {
        showSaveInterstitialAd(param = param)
    }

    private fun showUpscaleAd(param: UpscaleRequestParam) = viewModelScope.launch {
        _showUpscaleRewardAdEvent.emit(param)
    }

    private fun showSaveInterstitialAd(param: SaveRequestParam) = viewModelScope.launch {
        _showSaveInterstitialAdEvent.emit(param)
    }

    fun onCompleteShowedSaveInterstitialAdmob(param: SaveRequestParam) {
        saveImage(param = param)
    }

    fun onAdLoadRetryFailed(adError: LoadAdError) = viewModelScope.launch {
        // TODO #4 record Exception
        Logger.printLog("adError: $adError")
        _toast.emit(ResString(R.string.toast_check_network_error))
    }

    fun onSaveComplete() = viewModelScope.launch {
        _toast.emit(ResString(R.string.toast_complete_save))
    }

    fun onSaveFailed(e: Exception) = viewModelScope.launch {
        onErrorState(message = e.toString())
        _toast.emit(ResString(R.string.toast_failed_save))
    }

    fun onClickHelp(languageCode: Locale) = viewModelScope.launch {
        _redirectToWebBrowserEvent.emit(
            if (languageCode.language.contains("ko")) {
                HelpLink.KOREAN_HELP_LINK
            } else {
                HelpLink.GLOBAL_HELP_LINK
            }
        )
    }

    fun onFailRedirectToWebBrowser() = viewModelScope.launch {
        _toast.emit(ResString(R.string.toast_check_web_browser_app))
    }

    private fun Bitmap.toMultiPartBody(): MultipartBody.Part {
        val byteArrayOutputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val requestFile = byteArray.toRequestBody("image/png".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image_file", "image.png", requestFile)
    }
}

data class UpscaleRequestParam(
    val before: Bitmap,
    val hasFace: Boolean
)

data class SaveRequestParam(
    val after: Bitmap
)
