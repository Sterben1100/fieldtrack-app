package com.aswfoods.fieldtrack.ui.screens.login

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.data.remote.dto.LoginRequest
import com.aswfoods.fieldtrack.data.remote.dto.UserProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State UI untuk layar otentikasi login surveyor.
 */
@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    val isDemoMode: Boolean = false,
    val baseUrl: String = SessionManager.DEFAULT_BASE_URL,
    val isConnectionError: Boolean = false,
    val userProfile: UserProfileDto? = null,
    val token: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel pengelola interaksi login, pengaturan URL server, Mode Demo, dan persistensi sesi terenkripsi.
 */
@Stable
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val isDemo = sessionManager.isDemoMode()
        val savedBaseUrl = sessionManager.getBaseUrl()
        val savedToken = sessionManager.getToken()
        val savedProfile = sessionManager.getUserProfile()
        RetrofitClient.configure(
            url = savedBaseUrl,
            demoProvider = { sessionManager.isDemoMode() },
            provider = { sessionManager.getToken() }
        )
        _uiState.value = LoginUiState(
            isDemoMode = isDemo,
            baseUrl = savedBaseUrl,
            userProfile = if (!savedToken.isNullOrBlank()) savedProfile else null,
            token = if (savedProfile != null) savedToken else null
        )
    }

    /**
     * Mengaktifkan atau menonaktifkan Mode Demo (simulasi API lokal tanpa server backend).
     *
     * @param enabled true untuk mengaktifkan Mode Demo
     */
    fun setDemoMode(enabled: Boolean) {
        sessionManager.setDemoMode(enabled)
        RetrofitClient.configure(
            url = sessionManager.getBaseUrl(),
            demoProvider = { sessionManager.isDemoMode() },
            provider = { sessionManager.getToken() }
        )
        _uiState.value = _uiState.value.copy(
            isDemoMode = enabled,
            isConnectionError = false,
            errorMessage = null
        )
    }

    /**
     * Memperbarui alamat base URL server backend (berguna saat uji di HP fisik via IP Wi-Fi LAN).
     *
     * @param url Alamat host server (contoh: http://192.168.1.10:8080/)
     */
    fun updateBaseUrl(url: String) {
        sessionManager.setBaseUrl(url)
        val normalized = sessionManager.getBaseUrl()
        RetrofitClient.configure(
            url = normalized,
            demoProvider = { sessionManager.isDemoMode() },
            provider = { sessionManager.getToken() }
        )
        _uiState.value = _uiState.value.copy(
            baseUrl = normalized,
            isConnectionError = false,
            errorMessage = null
        )
    }

    /**
     * Mengaktifkan Mode Demo lalu langsung menjalankan login agar pengguna dapat masuk dalam 1 ketukan
     * ketika server backend belum aktif.
     *
     * @param username Username saat ini (jika kosong memakai `leader01`)
     * @param password Kata sandi saat ini (jika kosong memakai `password123`)
     */
    fun enableDemoAndLogin(username: String, password: String) {
        setDemoMode(true)
        val targetUser = username.trim().ifBlank { "leader01" }
        val targetPass = password.ifBlank { "password123" }
        login(targetUser, targetPass)
    }

    /**
     * Mengirimkan kredensial login surveyor ke server dan menyimpan sesi terenkripsi jika berhasil.
     *
     * @param username ID unik login
     * @param password Kata sandi
     */
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isConnectionError = false,
                errorMessage = "Username dan kata sandi wajib diisi."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isConnectionError = false,
                errorMessage = null
            )
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username.trim(), password))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!.data

                    // Simpan token dan profil ke penyimpanan terenkripsi Android Keystore
                    sessionManager.saveSession(authData.token, authData.user)
                    RetrofitClient.configure(
                        url = sessionManager.getBaseUrl(),
                        demoProvider = { sessionManager.isDemoMode() },
                        provider = { sessionManager.getToken() }
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnectionError = false,
                        userProfile = authData.user,
                        token = authData.token
                    )
                } else {
                    val serverMsg = if (_uiState.value.isDemoMode) {
                        "Login demo gagal. Gunakan kata sandi: password123 (atau 123456)."
                    } else {
                        "Login gagal. Periksa kembali username dan kata sandi Anda."
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnectionError = false,
                        errorMessage = serverMsg
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnectionError = true,
                    errorMessage = "Tidak dapat terhubung ke server (${sessionManager.getBaseUrl()}). " +
                        "Jika backend belum aktif, nyalakan Mode Demo di bawah."
                )
            }
        }
    }

    /**
     * Menghapus pesan error validasi atau kegagalan login dari UI state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            isConnectionError = false,
            errorMessage = null
        )
    }

    /**
     * Mengakhiri sesi surveyor aktif, menghapus penyimpanan sesi terenkripsi, dan mereset state login.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Upayakan revokasi token di server apabila koneksi tersedia
                RetrofitClient.apiService.logout()
            } catch (_: Exception) {
                // Abaikan error jaringan agar surveyor tetap dapat keluar saat offline
            } finally {
                sessionManager.clearSession()
                val savedUrl = sessionManager.getBaseUrl()
                RetrofitClient.configure(
                    url = savedUrl,
                    demoProvider = { sessionManager.isDemoMode() },
                    provider = { sessionManager.getToken() }
                )
                _uiState.value = LoginUiState(
                    isDemoMode = sessionManager.isDemoMode(),
                    baseUrl = savedUrl
                )
            }
        }
    }
}
