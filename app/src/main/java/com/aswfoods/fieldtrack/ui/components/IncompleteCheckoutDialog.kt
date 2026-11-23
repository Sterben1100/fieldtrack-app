package com.aswfoods.fieldtrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswfoods.fieldtrack.ui.theme.AswBorder
import com.aswfoods.fieldtrack.ui.theme.AswCharcoal
import com.aswfoods.fieldtrack.ui.theme.AswErrorRed
import com.aswfoods.fieldtrack.ui.theme.AswPendingOrange
import com.aswfoods.fieldtrack.ui.theme.AswPrimaryRed
import com.aswfoods.fieldtrack.ui.theme.AswSecondaryText
import com.aswfoods.fieldtrack.ui.theme.AswSurfaceLight

/**
 * Daftar kode alasan kendala operasional standar saat surveyor melakukan Check-Out Dini
 * sebelum survei terisi lengkap (mengacu pada Bab 5 memory_mobile 1.md).
 *
 * @property code Kode string yang dikirim ke field `incomplete_reason_type` pada API
 * @property title Label utama pilihan alasan
 * @property subtitle Keterangan tambahan situasi di lapangan (opsional)
 */
enum class IncompleteReasonType(
    val code: String,
    val title: String,
    val subtitle: String?
) {
    PIC_ABSENT(
        code = "pic_absent",
        title = "1. Pemilik / PIC Toko Tidak Berada di Tempat",
        subtitle = "Keluar kota, toko dijaga kasir pengganti / tidak berwenang"
    ),
    BUSY_STORE(
        code = "busy_store",
        title = "2. Toko Sedang Sangat Ramai Dikunjungi Pembeli",
        subtitle = "Pemilik / kasir sedang sibuk melayani transaksi pelanggan"
    ),
    STORE_CLOSED(
        code = "store_closed",
        title = "3. Toko Tutup / Sedang Renovasi / Tidak Beroperasi Sementara",
        subtitle = null
    ),
    REFUSED(
        code = "refused",
        title = "4. Pemilik / Penanggung Jawab Toko Menolak Disurvei",
        subtitle = null
    ),
    EMERGENCY(
        code = "emergency",
        title = "5. Kondisi Darurat / Cuaca Buruk / Akses Jalan Terhalang",
        subtitle = null
    ),
    OTHER(
        code = "other",
        title = "6. Lainnya (Alasan Khusus Lainnya)",
        subtitle = null
    )
}

/**
 * Modal intersepsi Check-Out Dini ketika surveyor menekan tombol selesai/check-out
 * sementara sebagian atau seluruh butir survei masih belum lengkap.
 *
 * @param roleLabel Keterangan peran aktif atau konteks ("Leader" atau "Member")
 * @param onDismiss Callback saat surveyor membatalkan dan memilih melanjutkan isi survei
 * @param onConfirm Callback saat surveyor mengonfirmasi Check-Out Dini dengan reasonType dan reasonText valid
 */
@Composable
fun IncompleteCheckoutDialog(
    roleLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (reasonType: String, reasonText: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(IncompleteReasonType.PIC_ABSENT) }
    var customReasonText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Peringatan Survei Belum Lengkap",
                    tint = AswPendingOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Survei $roleLabel Belum Lengkap",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AswCharcoal
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Anda melakukan Check-Out namun data survei belum terisi penuh. Pilih alasan kendala operasional di lapangan:",
                    fontSize = 12.sp,
                    color = AswSecondaryText
                )

                Spacer(modifier = Modifier.height(10.dp))

                IncompleteReasonType.entries.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(
                                color = if (isSelected) Color(0xFFFFF1F2) else AswSurfaceLight,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AswPrimaryRed else AswBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedReason = reason
                                validationError = null
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedReason = reason
                                validationError = null
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AswPrimaryRed)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reason.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = AswCharcoal
                            )
                            if (!reason.subtitle.isNullOrBlank()) {
                                Text(
                                    text = reason.subtitle,
                                    fontSize = 10.sp,
                                    color = AswSecondaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val isOther = selectedReason == IncompleteReasonType.OTHER
                OutlinedTextField(
                    value = customReasonText,
                    onValueChange = {
                        customReasonText = it
                        if (validationError != null) validationError = null
                    },
                    label = {
                        Text(
                            text = if (isOther) {
                                "Alasan Spesifik (Wajib min. 5 karakter)"
                            } else {
                                "Catatan Tambahan Kendala (Opsional)"
                            },
                            fontSize = 11.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Contoh: Pemilik minta datang kembali pukul 16:00 WIB",
                            fontSize = 11.sp
                        )
                    },
                    isError = validationError != null,
                    supportingText = validationError?.let { err ->
                        {
                            Text(
                                text = err,
                                color = AswErrorRed,
                                fontSize = 11.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AswPrimaryRed,
                        focusedLabelColor = AswPrimaryRed,
                        errorBorderColor = AswErrorRed,
                        errorLabelColor = AswErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBEB), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, AswPendingOrange, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Revisit",
                        tint = AswPendingOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Toko akan otomatis ditandai 'Perlu Kunjungan Ulang (Revisit)' pada rute jadwal berikutnya.",
                        fontSize = 11.sp,
                        color = AswCharcoal
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = customReasonText.trim()
                    if (selectedReason == IncompleteReasonType.OTHER && trimmed.length < 5) {
                        validationError = "Alasan khusus wajib diisi minimal 5 karakter."
                        return@Button
                    }
                    val finalReason = if (trimmed.isNotBlank()) {
                        trimmed
                    } else {
                        selectedReason.title.substringAfter(". ").trim()
                    }
                    onConfirm(selectedReason.code, finalReason)
                },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AswPrimaryRed,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Simpan & Check-Out",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(1.dp, AswBorder)
            ) {
                Text(
                    text = "Lanjutkan Isi Survei",
                    fontSize = 12.sp,
                    color = AswCharcoal
                )
            }
        }
    )
}
