package com.misaengfly.chordbox.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

const val BASE_URL = "http://35.193.88.18:3333"

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
    fun sendAudioFile(
        @Part file: MultipartBody.Part,
        @Part("token") token: String
    ): Call<FileResponse>

    @FormUrlEncoded
    @POST("/uploadUrl")
    fun sendYoutubeUrl(
        @Field("url") url: String,
        @Field("token") token: String
    ): Call<Unit>

    @FormUrlEncoded
    @POST("/fileChord")
    fun getRecordChord(
        @Field("filename") fileName: String,
        @Field("token") token: String
    ): Call<RecordResponse>

    @FormUrlEncoded
    @POST("/urlChord")
    fun getUrlChord(
        @Field("url") url: String,
        @Field("token") token: String
    ): Call<UrlResponse>
}

object FileApi {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}