package com.misaengfly.chordbox.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://192.168.0.2"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ApiService {
    @Multipart
    @POST("/uploadFile")
    fun sendAudioFile(@Part file: MultipartBody.Part): Call<FileResponse>

    @FormUrlEncoded
    @POST("/uploadUrl")
    fun sendYoutubeUrl(@Field("url") url: String): Call<Unit>
}

object FileApi {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}