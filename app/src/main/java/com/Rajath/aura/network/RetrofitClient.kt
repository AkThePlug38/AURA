package com.Rajath.aura.network

import android.util.Log
import com.Rajath.aura.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // router endpoint (ensure this is used)
    private const val BASE_URL = "https://router.huggingface.co/hf-inference/"

    private val authInterceptor = Interceptor { chain ->
        // debug: show token length (not value)
        Log.d("AURA-HF", "HF token length = ${BuildConfig.HF_API_KEY.length}")
        val request: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.HF_API_KEY}")
            .build()
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val req = chain.request()
            Log.d("AURA-HTTP", "REQUEST URL -> ${req.url}")
            val resp = chain.proceed(req)
            Log.d("AURA-HTTP", "RESPONSE CODE -> ${resp.code} for ${req.url}")
            resp
        }
        .build()

    val hfService: HuggingFaceService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuggingFaceService::class.java)
    }
}