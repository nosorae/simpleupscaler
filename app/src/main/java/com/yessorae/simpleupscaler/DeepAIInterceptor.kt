package com.yessorae.simpleupscaler

import okhttp3.Interceptor
import okhttp3.Response


class DeepAIInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("api-key", apiKey)
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }
}