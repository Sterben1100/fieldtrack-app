package com.aswfoods.fieldtrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswfoods.fieldtrack.ui.theme.AswErrorRed
import com.aswfoods.fieldtrack.ui.theme.AswPendingOrange
import com.aswfoods.fieldtrack.ui.theme.AswSuccessGreen
import com.aswfoods.fieldtrack.util.NetworkMonitor
import com.aswfoods.fieldtrack.util.NetworkQuality
import com.aswfoods.fieldtrack.util.NetworkStatus

/**
 * Komponen strip status jaringan terisolasi yang mengamati perubahan ping secara internal
 * sehingga pembaruan ping tidak memicu recomposition pada layar di bawahnya.
 */
@Composable
fun NetworkStatusBar() {
    val context = LocalContext.current
    val networkFlow = remember(context) {
        NetworkMonitor.observeNetworkStatus(context.applicationContext)
    }
    val status by networkFlow.collectAsState(initial = NetworkStatus())
    NetworkStatusBar(status = status)
}

/**
 * Strip pemberitahuan ringkas di bagian paling atas aplikasi untuk menampilkan latensi ping
 * dan kategori kualitas jaringan pengguna (`Good`, `Medium`, `Bad`, atau `Offline`).
 *
 * @param status Status jaringan dan angka ping terbaru
 */
@Composable
fun NetworkStatusBar(status: NetworkStatus) {
    val bgColor = when (status.quality) {
        NetworkQuality.GOOD -> Color(0xFFECFDF5)
        NetworkQuality.MEDIUM -> Color(0xFFFFFBEB)
        NetworkQuality.BAD -> Color(0xFFFEF2F2)
        NetworkQuality.OFFLINE -> Color(0xFFF3F4F6)
    }
    val accentColor = when (status.quality) {
        NetworkQuality.GOOD -> AswSuccessGreen
        NetworkQuality.MEDIUM -> AswPendingOrange
        NetworkQuality.BAD -> AswErrorRed
        NetworkQuality.OFFLINE -> Color(0xFF6B7280)
    }
    val textColor = when (status.quality) {
        NetworkQuality.GOOD -> Color(0xFF065F46)
        NetworkQuality.MEDIUM -> Color(0xFF92400E)
        NetworkQuality.BAD -> Color(0xFF991B1B)
        NetworkQuality.OFFLINE -> Color(0xFF374151)
    }

    val statusText = if (status.pingMs != null) {
        "Jaringan: ${status.quality.label}  •  Ping: ${status.pingMs} ms"
    } else {
        "Jaringan: Offline (Mode Lokal Aktif)"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color = accentColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
