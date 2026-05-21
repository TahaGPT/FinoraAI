package com.finora.ai.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit configuration for FinoraAI backend.
 *
 * In production, [BASE_URL] should point to the Railway deployment URL.
 * For local dev (emulator), use http://10.0.2.2:8000/ which maps to host machine's localhost.
 */
object ApiConfig {

    // ── Change this to your Railway URL after deployment ─────────
    // Local emulator:  "http://10.0.2.2:8000/"
    // Local device:    "http://<YOUR_PC_IP>:8000/"
    // Railway:         "https://your-app.up.railway.app/"
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)    // Agent pipeline can take time
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: FinoraApiService = retrofit.create(FinoraApiService::class.java)
}
