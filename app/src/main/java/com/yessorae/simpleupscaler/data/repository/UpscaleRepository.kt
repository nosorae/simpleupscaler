package com.yessorae.simpleupscaler.data.repository

import android.util.Log
import com.yessorae.simpleupscaler.data.PicWishApiService
import com.yessorae.simpleupscaler.data.model.Data
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import javax.inject.Inject

class UpscaleRepository @Inject constructor(
    private val retrofit: Retrofit
) {
    suspend fun upscaleImage(
        imageFile: MultipartBody.Part,
        type: RequestBody,
        sync: RequestBody,
        returnType: RequestBody
    ): Data? {
        val service = retrofit.create(PicWishApiService::class.java)
        val response = service.upscaleImage(
            imageFile = imageFile,
            type = type,
            sync = sync,
            returnType = returnType
        )

        return if (response.isSuccessful) {
            val jsonResponse = response.body()
            jsonResponse?.data
        } else {
            null
        }
    }
}
