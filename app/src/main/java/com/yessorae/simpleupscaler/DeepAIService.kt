package com.yessorae.simpleupscaler

import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Header

interface DeepAiService {
    @Multipart
    @POST("https://api.deepai.org/api/torch-srgan")
    fun upscaleImage(
        @Part("image\"; filename=\"image.png\" ") image: RequestBody,
        @Header("api-key") apiKey: String
    ): Call<ResponseBody>
}
