package com.yessorae.simpleupscaler.ui.util

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.yessorae.simpleupscaler.common.Logger.printLog

class LoggedFullScreenContentCallback(
    val type: String,
    val onSuccessShow: () -> Unit = {},
    val onFailedShow: (adError: AdError) -> Unit = {}
): FullScreenContentCallback() {
    override fun onAdClicked() {
        super.onAdClicked()
        printLog("$type : onAdClicked")
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        printLog("$type : onAdDismissedFullScreenContent")
    }

    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
        super.onAdFailedToShowFullScreenContent(p0)
        printLog("$type : onAdFailedToShowFullScreenContent")
        onFailedShow(p0)
    }

    override fun onAdImpression() {
        super.onAdImpression()
        printLog("$type : onAdImpression")
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        printLog("$type : onAdShowedFullScreenContent")
        onSuccessShow()
    }
}