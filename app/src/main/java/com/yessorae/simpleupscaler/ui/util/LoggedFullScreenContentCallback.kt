package com.yessorae.simpleupscaler.ui.util

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.yessorae.simpleupscaler.common.EVENT_CLICK_AD_ARG
import com.yessorae.simpleupscaler.common.EVENT_FAIL_AD_ARG
import com.yessorae.simpleupscaler.common.EVENT_SHOW_AD_ARG
import com.yessorae.simpleupscaler.common.Logger
import com.yessorae.simpleupscaler.ui.model.FullScreenAdType

class LoggedFullScreenContentCallback(
    val type: FullScreenAdType,
    val onSuccessShow: () -> Unit = {},
    val onFailedShow: (adError: AdError) -> Unit = {}
) : FullScreenContentCallback() {
    override fun onAdClicked() {
        super.onAdClicked()
        Logger.event(EVENT_CLICK_AD_ARG.format(type.gaArg))
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
    }

    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
        super.onAdFailedToShowFullScreenContent(p0)
        Logger.recordAdException(p0)
        Logger.event(EVENT_FAIL_AD_ARG.format(type.gaArg))
        onFailedShow(p0)
    }

    override fun onAdImpression() {
        super.onAdImpression()
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        Logger.event(EVENT_SHOW_AD_ARG.format(type.gaArg))
        onSuccessShow()
    }
}
