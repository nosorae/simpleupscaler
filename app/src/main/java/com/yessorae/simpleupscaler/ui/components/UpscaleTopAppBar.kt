package com.yessorae.simpleupscaler.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.ui.util.BasePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpscaleTopAppBar(onClickSettings: () -> Unit, onClickHelp: () -> Unit) {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null
            )
        },
        actions = {
            IconButton(onClick = onClickSettings) {
                Icon(Icons.Filled.Settings, contentDescription = null)
            }
            IconButton(onClick = onClickHelp) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null
                )
            }
        }
    )
}

@Preview
@Composable
fun ActionToolbarPreview() {
    BasePreview {
        UpscaleTopAppBar(onClickSettings = {}, onClickHelp = {})
    }
}