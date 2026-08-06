package com.vijaykumawat.Leyaa.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class FcmPayload(
    val token: String,
    val title: String,
    val body: String
)

interface FcmApiService {
    @POST("sendNotification")
    fun sendNotification(@Body payload: FcmPayload): Call<Void>
}
