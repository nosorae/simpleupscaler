package com.yessorae.simpleupscaler.ui.util

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yessorae.simpleupscaler.ui.model.StringModel

@Composable
fun getActivity() = LocalContext.current as ComponentActivity

fun Context.showToast(stringModel: StringModel) {
    Toast.makeText(this, stringModel.get(this), Toast.LENGTH_LONG).show()
}
