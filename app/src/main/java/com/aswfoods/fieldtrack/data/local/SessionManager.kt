package com.aswfoods.fieldtrack.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.aswfoods.fieldtrack.data.remote.dto.UserProfileDto
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Pengelola sesi otentikasi surveyor berbasis enkripsi Android Keystore (AES-256-GCM).
 * Menjamin token Bearer Sanctum dan profil pengguna tersimpan persisten secara aman
 * untuk mendukung operasi offline-first dan sinkronisasi latar belakang WorkManager.
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "fieldtrack_secure_session"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "fieldtrack_session_aes_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128

        private const val KEY_ENCRYPTED_TOKEN = "encrypted_token"
        private const val KEY_ENCRYPTED_PROFILE = "encrypted_profile"
        private const val KEY_DEMO_MODE = "demo_mode_enabled"
        private const val KEY_BASE_URL = "server_base_url_v5"
        const val WIFI_BASE_URL = "http://192.168.89.178:8000/"
        const val LAN_BASE_URL = "http://192.168.88.64:8000/"
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8000/"
        const val DEFAULT_BASE_URL = WIFI_BASE_URL

        /**
         * Menormalkan URL dasar backend agar selalu berakhir dengan `/` dan bebas duplikasi `/api/v1`.
         */
        fun normalizeBaseUrl(rawUrl: String): String {
            var cleaned = rawUrl.trim()
            if (cleaned.isEmpty()) return DEFAULT_BASE_URL
            if (!cleaned.contains("://")) {
                cleaned = "http://$cleaned"
            }
            cleaned = cleaned.trimEnd('/')
            if (cleaned.endsWith("/api/v1", ignoreCase = true)) {
                cleaned = cleaned.dropLast("/api/v1".length).trimEnd('/')
            }
            return "$cleaned/"
        }
    }

    /**
     * Menyimpan URL server backend (berguna saat uji di HP fisik via IP Wi-Fi/LAN).
     *
     * @param url Alamat base URL server (misal: http://192.168.89.178:8080/)
     */
    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, normalizeBaseUrl(url)).apply()
    }

    /**
     * Mengambil URL server backend aktif yang tersimpan.
     *
     * @return Alamat base URL server
     */
    fun getBaseUrl(): String =
        prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL)?.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL

    /**
     * Mengaktifkan atau menonaktifkan Mode Demo (Mock API lokal tanpa server backend).
     *
     * @param enabled true untuk memakai MockApiInterceptor, false untuk server asli
     */
    fun setDemoMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply()
    }

    /**
     * Memeriksa apakah aplikasi sedang berjalan dalam Mode Demo lokal.
     *
     * @return true jika Mode Demo aktif
     */
    fun isDemoMode(): Boolean = prefs.getBoolean(KEY_DEMO_MODE, false)

    /**
     * Menyimpan token autentikasi dan profil surveyor dalam bentuk terenkripsi.
     *
     * @param token Bearer token dari endpoint login
     * @param user Profil pengguna yang sedang aktif
     */
    fun saveSession(token: String, user: UserProfileDto) {
        val encryptedToken = encryptText(token)
        val encryptedProfile = encryptText(gson.toJson(user))
        prefs.edit()
            .putString(KEY_ENCRYPTED_TOKEN, encryptedToken)
            .putString(KEY_ENCRYPTED_PROFILE, encryptedProfile)
            .apply()
    }

    /**
     * Mengambil token autentikasi terdekripsi dari penyimpanan lokal.
     *
     * @return Token Bearer atau null jika belum login / gagal dekripsi
     */
    fun getToken(): String? {
        val raw = prefs.getString(KEY_ENCRYPTED_TOKEN, null) ?: return null
        return decryptText(raw)
    }

    /**
     * Mengambil profil surveyor tersimpan dari penyimpanan lokal.
     *
     * @return Objek UserProfileDto atau null jika sesi tidak tersedia
     */
    fun getUserProfile(): UserProfileDto? {
        val raw = prefs.getString(KEY_ENCRYPTED_PROFILE, null) ?: return null
        val json = decryptText(raw) ?: return null
        return runCatching { gson.fromJson(json, UserProfileDto::class.java) }.getOrNull()
    }

    /**
     * Memeriksa apakah perangkat masih memiliki sesi login aktif.
     *
     * @return true jika token dan profil pengguna tersedia
     */
    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank() && getUserProfile() != null

    /**
     * Menghapus data sesi autentikasi terenkripsi saat surveyor keluar (logout)
     * tanpa mereset preferensi Mode Demo yang dipilih pengguna.
     */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_ENCRYPTED_TOKEN)
            .remove(KEY_ENCRYPTED_PROFILE)
            .apply()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptText(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val ivEncoded = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataEncoded = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
        return "$ivEncoded:$dataEncoded"
    }

    private fun decryptText(payload: String): String? {
        return runCatching {
            val parts = payload.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        }.getOrNull()
    }
}

