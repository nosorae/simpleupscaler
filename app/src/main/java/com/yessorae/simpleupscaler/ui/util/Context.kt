package com.yessorae.simpleupscaler.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yessorae.simpleupscaler.common.Logger
import com.yessorae.simpleupscaler.ui.model.StringModel
import java.util.Locale

@Composable
fun getActivity() = LocalContext.current as ComponentActivity

fun Context.showToast(stringModel: StringModel) {
    Toast.makeText(this, stringModel.get(this), Toast.LENGTH_LONG).show()
}

fun Context.redirectToWebBrowser(link: String, onActivityNotFoundException: () -> Unit) {
    Intent(Intent.ACTION_VIEW, Uri.parse(link)).also { intent ->
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            onActivityNotFoundException()
            Logger.recordException(e)
        } catch (e: Exception) {
            Logger.recordException(e)
        }
    }
}

fun Context.getSettingsLocale(): Locale {
    val config = android.content.res.Configuration()
    Settings.System.getConfiguration(this.contentResolver, config)
    return config.locales.get(0)
}
