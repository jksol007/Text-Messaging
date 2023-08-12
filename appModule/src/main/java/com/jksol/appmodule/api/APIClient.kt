package com.jksol.appmodule.api

import com.google.gson.GsonBuilder
import com.jksol.appmodule.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class APIClient {

    companion object {
        private var retrofit: Retrofit? = null

        fun getClient(): Retrofit? {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val gson = GsonBuilder()
                .setLenient()
                .create()
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL1)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
            return retrofit
        }

        fun getClient1(): Retrofit? {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val gson = GsonBuilder()
                .setLenient()
                .create()
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL2)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
            return retrofit
        }
    }

}