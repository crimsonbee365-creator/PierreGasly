package com.pierregasly.app.data.api

import com.google.gson.GsonBuilder
import com.pierregasly.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Supabase client using REST + Auth HTTP endpoints.
 * IMPORTANT: Uses ANON key only (safe for mobile with proper RLS).
 */
object SupabaseClient {

    private val gson = GsonBuilder().setLenient().create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val isConfigured: Boolean
        get() {
            val url = BuildConfig.SUPABASE_URL.trim()
            val anon = BuildConfig.SUPABASE_ANON_KEY.trim()
            val urlLooksValid = url.startsWith("https://") && ".supabase.co" in url
            val anonLooksValid = anon.count { it == '.' } == 2 && anon.length > 80
            return urlLooksValid && anonLooksValid
        }

    private val apikeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val hasAuthHeader = !original.header("Authorization").isNullOrBlank()

        val req = original.newBuilder()
            .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
            .apply {
                if (!hasAuthHeader) {
                    header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                }
            }
            .build()
        chain.proceed(req)
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(apikeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val auth: SupabaseAuthService = retrofit.create(SupabaseAuthService::class.java)
    val rest: SupabaseRestService = retrofit.create(SupabaseRestService::class.java)
}
