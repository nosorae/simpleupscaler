package com.yessorae.simpleupscaler.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.yessorae.simpleupscaler.BuildConfig
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.common.Logger
import com.yessorae.simpleupscaler.ui.components.ActionButton
import com.yessorae.simpleupscaler.ui.components.ActionButtonWithAd
import com.yessorae.simpleupscaler.ui.components.AdmobBanner
import com.yessorae.simpleupscaler.ui.components.EmptyImage
import com.yessorae.simpleupscaler.ui.components.ImageComparer
import com.yessorae.simpleupscaler.ui.components.OutlinedActionButton
import com.yessorae.simpleupscaler.ui.components.SingleImage
import com.yessorae.simpleupscaler.ui.components.UpscaleTopAppBar
import com.yessorae.simpleupscaler.ui.model.FullScreenAdType
import com.yessorae.simpleupscaler.ui.model.UpscaleScreenState
import com.yessorae.simpleupscaler.ui.theme.Dimen
import com.yessorae.simpleupscaler.ui.util.IntentUtil
import com.yessorae.simpleupscaler.ui.util.LoggedFullScreenContentCallback
import com.yessorae.simpleupscaler.ui.util.getActivity
import com.yessorae.simpleupscaler.ui.util.getSettingsLocale
import com.yessorae.simpleupscaler.ui.util.redirectToWebBrowser
import com.yessorae.simpleupscaler.ui.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: UpscaleViewModel = viewModel()
) {
    val state by viewModel.screenState.collectAsState()

    val activity = getActivity()

    var adLoading by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = Unit) {
        launch(Dispatchers.Main) {
            viewModel.showUpscaleRewardAdEvent.collectLatest { param ->
                adLoading = true
                loadUpscaleRewardAdRequest(
                    activity,
                    onAdLoaded = { upscaleRewardedAd ->
                        adLoading = false
                        upscaleRewardedAd.fullScreenContentCallback =
                            LoggedFullScreenContentCallback(type = FullScreenAdType.UPSCALE_REWARD)
                        upscaleRewardedAd.show(activity) {
                            viewModel.onCompleteUpscaleRewardAdmob(param = param)
                        }
                    },
                    onLoadFailed = { adError ->
                        viewModel.onAdLoadRetryFailed(adError = adError)
                    }
                )
            }
        }

        launch(Dispatchers.Main) {
            viewModel.showSaveInterstitialAdEvent.collectLatest { param ->
                adLoading = true
                loadSaveInterstitialAdRequest(
                    activity,
                    onAdLoaded = { interstitialAd ->
                        adLoading = false
                        interstitialAd.fullScreenContentCallback =
                            LoggedFullScreenContentCallback(
                                type = FullScreenAdType.SAVE_INTERSTITIAL,
                                onSuccessShow = {
                                    viewModel.onCompleteShowedSaveInterstitialAdmob(param = param)
                                }
                            )
                        interstitialAd.show(activity)
                    },
                    onLoadFailed = { adError ->
                        viewModel.onAdLoadRetryFailed(adError = adError)
                    }
                )
            }
        }

        launch {
            viewModel.toast.collectLatest { stringModel ->
                activity.showToast(stringModel)
            }
        }

        launch(Dispatchers.IO) {
            viewModel.saveImageEvent.collectLatest { saveParam ->
                try {
                    saveImageUrlToGallery(context = activity, bitmap = saveParam.after)
                    viewModel.onSaveComplete()
                } catch (e: Exception) {
                    viewModel.onSaveFailed(e = e)
                }
            }
        }

        launch {
            viewModel.redirectToWebBrowserEvent.collectLatest { link ->
                activity.redirectToWebBrowser(
                    link = link,
                    onActivityNotFoundException = {
                        viewModel.onFailRedirectToWebBrowser()
                    }
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            UpscaleTopAppBar(
                onClickHelp = {
                    viewModel.onClickHelp(activity.getSettingsLocale())
                }
            )
        },
        bottomBar = {
            BottomAdmobBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { innerPadding ->
        BodyScreen(
            modifier = Modifier.padding(innerPadding),
            state = state,
            onSelectImage = { before ->
                viewModel.onSelectImage(before)
            },
            onClickUpscaleImage = { before, hasFace, retry ->
                viewModel.onClickRequestUpscale(
                    UpscaleRequestParam(
                        before = before,
                        hasFace = hasFace,
                        retry = retry
                    )
                )
            },
            onClickSave = { after ->
                viewModel.onClickRequestSave(
                    SaveRequestParam(after = after)
                )
            }
        )

        if (adLoading) {
            AdLoadingScreen()
        }
    }
}

private fun loadUpscaleRewardAdRequest(
    activity: ComponentActivity,
    onAdLoaded: (RewardedInterstitialAd) -> Unit,
    onLoadFailed: (adError: LoadAdError) -> Unit
) {
    val adRequest = AdRequest.Builder().build()
    RewardedInterstitialAd.load(
        activity,
        BuildConfig.ADMOB_REWARD_FULL_PAGE_ID,
        adRequest,
        object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                onLoadFailed(adError)
            }
        }
    )
}

private fun loadSaveInterstitialAdRequest(
    activity: ComponentActivity,
    onAdLoaded: (InterstitialAd) -> Unit,
    onLoadFailed: (adError: LoadAdError) -> Unit
) {
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
        activity,
        BuildConfig.ADMOB_FULL_PAGE_ID,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onLoadFailed(adError)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                onAdLoaded(interstitialAd)
            }
        }
    )
}

@Composable
fun BodyScreen(
    modifier: Modifier = Modifier,
    state: UpscaleScreenState,
    onSelectImage: (before: Bitmap) -> Unit,
    onClickUpscaleImage: (after: Bitmap, hasFace: Boolean, retry: Boolean) -> Unit,
    onClickSave: (after: Bitmap) -> Unit
) {
    val context = LocalContext.current

    val takePhotoFromAlbumLauncher = // 갤러리에서 사진 가져오기
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    uriToBitmap(context = context, selectedFileUri = uri)?.let { bitmap ->
                        onSelectImage(bitmap)
                    } ?: run {
                        Logger.recordCustomException("uriToBitmap is null")
                    }
                } ?: run {
                    Logger.recordCustomException("ActivityResult.data is null")
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
                    retry = state.retry,
                    onClickReselectImage = {
                        takePhotoFromAlbumLauncher.launch(IntentUtil.createGalleryIntent())
                    },
                    onClickUpscaleImage = { before, hasFace ->
                        onClickUpscaleImage(before, hasFace, state.retry)
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
                        onClickSave(after)
                    }
                )
            }

            else -> {
                // do nothing
            }
        }
    }
}

@Composable
fun ColumnScope.StartScreen(
    onClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(Dimen.space_16))

    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource()
            ),
        contentAlignment = Alignment.Center
    ) {
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
    retry: Boolean,
    onClickReselectImage: () -> Unit,
    onClickUpscaleImage: (before: Bitmap, hasFace: Boolean) -> Unit
) {
    var hasFace by remember {
        mutableStateOf(true)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
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

        Spacer(modifier = Modifier.width(Dimen.space_4))

        Switch(
            checked = hasFace,
            onCheckedChange = {
                hasFace = it
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
        if (retry) {
            ActionButton(
                text = stringResource(id = R.string.common_enhance_image),
                onClick = { onClickUpscaleImage(selectedImage, hasFace) },
                modifier = Modifier.weight(1f)
            )
        } else {
            ActionButtonWithAd(
                text = stringResource(id = R.string.common_enhance_image),
                onClick = { onClickUpscaleImage(selectedImage, hasFace) },
                modifier = Modifier.weight(1f)
            )
        }
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
fun AdLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = {}, enabled = false),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun BottomAdmobBanner(modifier: Modifier = Modifier) {
    AdmobBanner(modifier = modifier, adId = BuildConfig.ADMOB_BOTTOM_BANNER_ID)
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
        inputStream.close()
        image
    } catch (e: IOException) {
        Logger.recordException(e)
        e.printStackTrace()
        null
    }
}

private fun saveImageUrlToGallery(context: Context, bitmap: Bitmap) {
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
