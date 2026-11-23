package com.aswfoods.fieldtrack.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.ui.theme.AswBorder
import com.aswfoods.fieldtrack.ui.theme.AswCharcoal
import com.aswfoods.fieldtrack.ui.theme.AswErrorRed
import com.aswfoods.fieldtrack.ui.theme.AswPendingOrange
import com.aswfoods.fieldtrack.ui.theme.AswPrimaryRed
import com.aswfoods.fieldtrack.ui.theme.AswSecondaryText
import com.aswfoods.fieldtrack.ui.theme.AswSurfaceLight

/**
 * Layar masuk surveyor dengan toggle Mode Demo (tanpa server), tombol lihat/sembunyikan kata sandi,
 * serta pengaturan URL server untuk pengujian pada perangkat fisik via Wi-Fi LAN.
 *
 * @param viewModel ViewModel pengelola otentikasi dan konfigurasi server
 * @param onLoginSuccess Callback navigasi ke Dashboard setelah autentikasi berhasil
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (role: String, userName: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showServerSettings by remember { mutableStateOf(false) }
    var serverUrlInput by remember(state.baseUrl) { mutableStateOf(state.baseUrl) }

    LaunchedEffect(state.userProfile) {
        state.userProfile?.let {
            onLoginSuccess(it.role, it.name)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Brand ASW Foods
            Text(
                text = "ASW FOODS",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AswPrimaryRed,
                letterSpacing = 1.sp
            )
            Text(
                text = "FieldTrack Pro",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = AswCharcoal
            )
            Text(
                text = "Sistem Audit & Pemantauan Toko Terpadu",
                fontSize = 12.sp,
                color = AswSecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Kartu Formulir Login
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AswSurfaceLight),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AswBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (state.errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Username Surveyor") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AswPrimaryRed,
                            focusedLabelColor = AswPrimaryRed
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (state.errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Kata Sandi") },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Sembunyikan kata sandi"
                                    } else {
                                        "Lihat kata sandi"
                                    },
                                    tint = AswSecondaryText
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AswPrimaryRed,
                            focusedLabelColor = AswPrimaryRed
                        ),
                        singleLine = true
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = AswErrorRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        if (state.isConnectionError && !state.isDemoMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.enableDemoAndLogin(username, password) },
                                shape = RoundedCornerShape(percent = 50),
                                border = BorderStroke(1.dp, AswPendingOrange)
                            ) {
                                Text(
                                    text = "Aktifkan Mode Demo & Masuk",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AswPendingOrange
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tombol Pill CTA Utama
                    Button(
                        onClick = { viewModel.login(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AswPrimaryRed,
                            contentColor = Color.White
                        ),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.height(22.dp)
                            )
                        } else {
                            Text(
                                text = "Masuk ke Lapangan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Panel Toggle Mode Demo (Simulasi Backend Lokal)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isDemoMode) Color(0xFFFFFBEB) else AswSurfaceLight
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (state.isDemoMode) AswPendingOrange else AswBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Demo (Tanpa Server)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AswCharcoal
                            )
                            Text(
                                text = "Nyalakan ini di HP fisik jika server backend belum berjalan.",
                                fontSize = 11.sp,
                                color = AswSecondaryText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = state.isDemoMode,
                            onCheckedChange = { viewModel.setDemoMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AswPrimaryRed
                            )
                        )
                    }

                    if (!state.isDemoMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showServerSettings) {
                                "▾ Tutup Pengaturan URL Server"
                            } else {
                                "▸ Server Live: ${state.baseUrl} (Ketuk ubah)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AswPrimaryRed,
                            modifier = Modifier.clickable { showServerSettings = !showServerSettings }
                        )
                        if (showServerSettings) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Preset Koneksi Cepat:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AswSecondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    "Wi-Fi\n192.168.89.178" to SessionManager.WIFI_BASE_URL,
                                    "LAN\n192.168.88.64" to SessionManager.LAN_BASE_URL,
                                    "Emulator\n10.0.2.2" to SessionManager.EMULATOR_BASE_URL
                                )
                                presets.forEach { (label, presetUrl) ->
                                    val isActive = state.baseUrl.equals(presetUrl, ignoreCase = true)
                                    OutlinedButton(
                                        onClick = {
                                            serverUrlInput = presetUrl
                                            viewModel.updateBaseUrl(presetUrl)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(
                                            width = if (isActive) 1.5.dp else 1.dp,
                                            color = if (isActive) AswPrimaryRed else AswBorder
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isActive) AswPrimaryRed else AswCharcoal,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = serverUrlInput,
                                onValueChange = { serverUrlInput = it },
                                label = { Text("Base URL Backend", fontSize = 11.sp) },
                                placeholder = { Text("http://192.168.89.178:8000/", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { viewModel.updateBaseUrl(serverUrlInput) },
                                shape = RoundedCornerShape(percent = 50),
                                border = BorderStroke(1.dp, AswCharcoal)
                            ) {
                                Text(
                                    text = "Simpan URL Server",
                                    fontSize = 11.sp,
                                    color = AswCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
