package com.example.nusa_rasa.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Ganti BASE_URL sesuai server FastAPI kamu:
     *  - Emulator Android   → "http://10.0.2.2:8000/"
     *  - Perangkat fisik    → "http://<IP_LAPTOP>:8000/"  (pastikan satu jaringan WiFi)
     *  - Produksi / ngrok   → "https://xxxx.ngrok.io/"
     */
    const val BASE_URL = "http://172.16.67.211:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
