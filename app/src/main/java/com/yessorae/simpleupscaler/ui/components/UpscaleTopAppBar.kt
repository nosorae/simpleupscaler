package com.yessorae.simpleupscaler.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yessorae.simpleupscaler.R
import com.yessorae.simpleupscaler.ui.util.BasePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpscaleTopAppBar(
    modifier: Modifier = Modifier,
    onClickHelp: () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = stringResource(id = R.string.cd_logo)
            )
        },
//        actions = {
//            IconButton(onClick = onClickHelp) {
//                Icon(
//                    imageVector = Icons.Outlined.HelpOutline,
//                    contentDescription = stringResource(id = R.string.cd_help)
//                )
//            }
//        }
    )
}

@Preview
@Composable
fun ActionToolbarPreview() {
    BasePreview {
        UpscaleTopAppBar()
    }
}
