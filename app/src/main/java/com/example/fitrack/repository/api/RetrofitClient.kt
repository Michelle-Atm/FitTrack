package com.example.fitrack.repository.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val OFF_BASE_URL = "https://world.openfoodfacts.org/"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .addInterceptor { chain ->
                var tentative = 0
                var reponse = chain.proceed(chain.request())
                while (!reponse.isSuccessful
                    && reponse.code in listOf(503, 429)
                    && tentative < 2) {
                    reponse.close()
                    tentative++
                    Thread.sleep(1000L * tentative)
                    reponse = chain.proceed(chain.request())
                }
                reponse
            }
            .build()
    }

    val openFoodFactsService: OpenFoodFactsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OFF_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApiService::class.java)
    }
}
