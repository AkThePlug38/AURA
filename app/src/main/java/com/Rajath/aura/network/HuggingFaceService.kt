package com.Rajath.aura.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

data class HFRequest(val inputs: String)
typealias HFResponse = List<Any>

interface HuggingFaceService {
    @Headers("Content-Type: application/json")
    @POST("models/{modelId}")
    suspend fun analyze(
        @Path(value = "modelId", encoded = true) modelId: String,
        @Body body: HFRequest
    ): Response<HFResponse>
}