package com.yessorae.simpleupscaler.data

import okhttp3.Interceptor
import okhttp3.Response

class PicWishApiInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-API-KEY", "wxf4j0x1k1dtchfmt")
            .build()
        return chain.proceed(request)
    }
}
