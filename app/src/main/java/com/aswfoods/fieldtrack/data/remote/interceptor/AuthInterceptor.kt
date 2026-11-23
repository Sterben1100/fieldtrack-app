package com.aswfoods.fieldtrack.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp untuk menyematkan token Bearer Sanctum pada setiap permintaan API yang terproteksi.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    /**
     * Mencegat request dan menambahkan header otorisasi jika token tersedia.
     *
     * @param chain Rantai eksekusi request OkHttp
     * @return Response HTTP
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

