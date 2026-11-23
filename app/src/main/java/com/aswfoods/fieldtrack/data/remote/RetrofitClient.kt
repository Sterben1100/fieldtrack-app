package com.aswfoods.fieldtrack.data.remote

import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.data.remote.api.FieldTrackApiService
import com.aswfoods.fieldtrack.data.remote.interceptor.AuthInterceptor
import com.aswfoods.fieldtrack.data.remote.interceptor.MockApiInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Penyedia instans Retrofit dan OkHttpClient singleton.
 * Mendukung perubahan URL server dinamis tanpa membuat ulang instans `apiService`
 * serta mendukung Mode Demo (`MockApiInterceptor`).
 */
object RetrofitClient {

    private val LOCALHOST_ALIASES = setOf("10.0.2.2", "localhost", "127.0.0.1")

    @Volatile
    private var baseUrl: String = SessionManager.DEFAULT_BASE_URL
    private var tokenProvider: () -> String? = { null }
    private var demoModeProvider: () -> Boolean = { false }

    /**
     * Konfigurasi dinamis untuk base URL, penyedia status Mode Demo, dan token penyedia sesi.
     *
     * @param url Alamat host server
     * @param demoProvider Fungsi lambda penentu apakah Mode Demo aktif
     * @param provider Fungsi lambda penyuplai bearer token
     */
    fun configure(
        url: String,
        demoProvider: () -> Boolean = demoModeProvider,
        provider: () -> String?
    ) {
        val normalizedUrl = SessionManager.normalizeBaseUrl(url)
        if (normalizedUrl.toHttpUrlOrNull() != null) {
            baseUrl = normalizedUrl
        }
        demoModeProvider = demoProvider
        tokenProvider = provider
    }

    /**
     * Mengubah host URL media lokal (`10.0.2.2`, `localhost`, `127.0.0.1`) dari server
     * menjadi host & port server aktif agar gambar SPV maupun planogram dapat dimuat di HP fisik.
     */
    fun rewriteLocalhostUrl(rawUrl: String): String {
        val parsed = rawUrl.trim().toHttpUrlOrNull() ?: return rawUrl
        if (parsed.host !in LOCALHOST_ALIASES) return rawUrl
        val target = baseUrl.toHttpUrlOrNull() ?: return rawUrl
        return parsed.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .build()
            .toString()
    }

    private val dynamicBaseUrlInterceptor = Interceptor { chain ->
        val original = chain.request()
        val targetUrl = baseUrl.toHttpUrlOrNull()
        val updatedRequest = if (targetUrl != null) {
            val newUrl = original.url.newBuilder()
                .scheme(targetUrl.scheme)
                .host(targetUrl.host)
                .port(targetUrl.port)
                .build()
            original.newBuilder().url(newUrl).build()
        } else {
            original
        }
        chain.proceed(updatedRequest)
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(MockApiInterceptor { demoModeProvider() })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val apiService: FieldTrackApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FieldTrackApiService::class.java)
    }
}
