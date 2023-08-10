package com.yessorae.simpleupscaler.data

import com.yessorae.simpleupscaler.data.model.Data
import com.yessorae.simpleupscaler.data.model.PicWishUpscaleResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
interface PicWishApiService {
    @Multipart
    @POST("api/tasks/visual/scale")
    suspend fun upscaleImage(
        @Part imageFile: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("sync") sync: RequestBody
    ): Response<PicWishUpscaleResult>
}