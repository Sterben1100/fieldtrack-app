package com.aswfoods.fieldtrack.ui.screens.dashboard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswfoods.fieldtrack.ui.theme.AswBorder
import com.aswfoods.fieldtrack.ui.theme.AswCharcoal
import com.aswfoods.fieldtrack.ui.theme.AswErrorRed
import com.aswfoods.fieldtrack.ui.theme.AswPendingOrange
import com.aswfoods.fieldtrack.ui.theme.AswPrimaryRed
import com.aswfoods.fieldtrack.ui.theme.AswSecondaryText
import com.aswfoods.fieldtrack.ui.theme.AswSuccessGreen
import com.aswfoods.fieldtrack.ui.theme.AswSurfaceLight

private val PillShape = RoundedCornerShape(percent = 50)
private val CardShape14 = RoundedCornerShape(14.dp)
private val CardShape12 = RoundedCornerShape(12.dp)
private val CardShape10 = RoundedCornerShape(10.dp)
private val CardShape8 = RoundedCornerShape(8.dp)
private val TagShape6 = RoundedCornerShape(6.dp)
private val BorderThin = BorderStroke(1.dp, AswBorder)
private val CtaPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

/**
 * Layar utama Dashboard Surveyor untuk memantau rute harian, progres kolaborasi pasangan tim,
 * status kunjungan per toko, dan mengelola sinkronisasi data offline-first.
 *
 * @param userName Nama surveyor yang sedang bertugas
 * @param role Peran aktif ("leader" atau "member")
 * @param viewModel ViewModel pengelola state Dashboard
 * @param onStartSurvey Callback saat surveyor memulai atau mengedit survei suatu toko
 * @param onLogout Callback saat surveyor keluar dari sesi aktif
 */
@Composable
fun DashboardScreen(
    userName: String,
    role: String,
    viewModel: DashboardViewModel,
    onStartSurvey: (outletId: Long) -> Unit,
    onLogout: () -> Unit
) {
    val outletItems by viewModel.outletItems.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val failedCount by viewModel.failedCount.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filterScrollState = rememberScrollState()
    val listScrollState = rememberScrollState()
    var selectedProfileOutletId by remember { mutableStateOf<Long?>(null) }
    val onSelectOutlet = remember { { outletId: Long -> selectedProfileOutletId = outletId } }

    LaunchedEffect(role) {
        viewModel.setActiveRole(role)
    }

    LaunchedEffect(uiState.message) {
        val currentMessage = uiState.message
        if (!currentMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = currentMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    val counts = remember(outletItems) {
        var belum = 0
        var pending = 0
        var selesai = 0
        var completed = 0
        for (item in outletItems) {
            when (item.visitStatus) {
                OutletVisitStatus.BELUM_DISURVEI, OutletVisitStatus.PERLU_REVISIT -> belum++
                OutletVisitStatus.PENDING -> {
                    pending++
                    completed++
                }
                OutletVisitStatus.GAGAL -> pending++
                OutletVisitStatus.TERSINKRON -> {
                    selesai++
                    completed++
                }
            }
        }
        intArrayOf(belum, pending, selesai, completed)
    }
    val belumCount = counts[0]
    val pendingTabCount = counts[1]
    val selesaiCount = counts[2]
    val completedCount = counts[3]

    val progressRatio = remember(completedCount, outletItems.size) {
        if (outletItems.isNotEmpty()) {
            completedCount.toFloat() / outletItems.size.toFloat()
        } else {
            0f
        }
    }

    val filteredItems = remember(outletItems, uiState.selectedFilter) {
        when (uiState.selectedFilter) {
            DashboardFilter.SEMUA -> outletItems
            DashboardFilter.BELUM -> outletItems.filter {
                it.visitStatus == OutletVisitStatus.BELUM_DISURVEI || it.visitStatus == OutletVisitStatus.PERLU_REVISIT
            }
            DashboardFilter.PENDING -> outletItems.filter {
                it.visitStatus == OutletVisitStatus.PENDING || it.visitStatus == OutletVisitStatus.GAGAL
            }
            DashboardFilter.SELESAI -> outletItems.filter { it.visitStatus == OutletVisitStatus.TERSINKRON }
        }
    }

    val activeProfileItem = remember(outletItems, selectedProfileOutletId) {
        selectedProfileOutletId?.let { targetId ->
            outletItems.firstOrNull { it.outlet.id == targetId }
        }
    }

    activeProfileItem?.let { profileItem ->
        OutletProfileDialog(
            item = profileItem,
            role = role,
            onDismiss = { selectedProfileOutletId = null },
            onStartSurvey = {
                selectedProfileOutletId = null
                onStartSurvey(profileItem.outlet.id)
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    containerColor = if (uiState.isError) AswErrorRed else AswCharcoal,
                    contentColor = Color.White,
                    shape = CardShape12
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Header Profil Surveyor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selamat Bertugas,",
                        fontSize = 12.sp,
                        color = AswSecondaryText
                    )
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (role.lowercase() == "leader") AswPrimaryRed else AswCharcoal,
                                    shape = PillShape
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ROLE: ${role.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (uiState.isDemoMode) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = AswPendingOrange,
                                        shape = PillShape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MODE DEMO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onLogout,
                    shape = PillShape,
                    border = BorderThin,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "Keluar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AswCharcoal)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ringkasan Progres Harian
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progres Kunjungan Hari Ini",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AswSecondaryText
                    )
                    Text(
                        text = "$completedCount / ${outletItems.size} Toko (${(progressRatio * 100).toInt()}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(PillShape),
                    color = AswPrimaryRed,
                    trackColor = AswBorder
                )
            }

            // 2. Banner Antrean Offline (hanya muncul bila ada data pending untuk di-push)
            if (pendingCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    shape = CardShape12,
                    border = BorderStroke(1.dp, AswPendingOrange)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sync",
                                tint = AswPendingOrange,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$pendingCount Data Survey Tertunda",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AswCharcoal
                                )
                                Text(
                                    text = "Tersimpan aman di memori lokal",
                                    fontSize = 11.sp,
                                    color = AswSecondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.pushPendingDataNow() },
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AswPendingOrange,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            enabled = !uiState.isSyncing
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Push Data",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (failedCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AswErrorRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Gagal Sync",
                                tint = AswErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$failedCount data gagal dikirim ke server. Periksa data atau coba lagi.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = AswErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.retryFailedData() },
                            shape = RoundedCornerShape(percent = 50),
                            border = BorderStroke(1.dp, AswErrorRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            enabled = !uiState.isSyncing
                        ) {
                            Text(
                                text = "Coba Lagi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AswErrorRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Header Rute & Tombol Unduh Ulang
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rute Toko Hari Ini (${filteredItems.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AswCharcoal
                )
                OutlinedButton(
                    onClick = { viewModel.syncMasterRoutes() },
                    shape = PillShape,
                    border = BorderThin,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    enabled = !uiState.isSyncing
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AswPrimaryRed
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = AswCharcoal,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Unduh Rute", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AswCharcoal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Filter Status Kunjungan dengan Jumlah & Border Tegas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardFilter.entries.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    val countLabel = when (filter) {
                        DashboardFilter.SEMUA -> outletItems.size
                        DashboardFilter.BELUM -> belumCount
                        DashboardFilter.PENDING -> pendingTabCount
                        DashboardFilter.SELESAI -> selesaiCount
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) AswPrimaryRed else Color.White,
                                shape = PillShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AswPrimaryRed else AswBorder,
                                shape = PillShape
                            )
                            .clickable { viewModel.setFilter(filter) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${filter.label} ($countLabel)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else AswCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Daftar Toko (Pre-composed Scroll Column untuk 8-15 toko harian agar nol jank saat scroll)
            if (filteredItems.isEmpty() && !uiState.isSyncing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (outletItems.isEmpty()) {
                            "Belum ada jadwal toko harian. Klik 'Unduh Rute'."
                        } else {
                            "Tidak ada toko pada filter '${uiState.selectedFilter.label}'."
                        },
                        color = AswSecondaryText,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(listScrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredItems.forEach { item ->
                        key(item.outlet.id) {
                            OutletItemCard(
                                item = item,
                                onSelectOutlet = onSelectOutlet
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Kartu ringkasan satu outlet pada jadwal kunjungan dengan hierarki visual bersih
 * dan informasi kolaborasi pasangan surveyor. Mengetuk kartu akan membuka menu Profil Toko.
 *
 * @param item Gabungan data toko dan status kunjungan lokal/server
 * @param onSelectOutlet Callback dengan ID toko saat kartu outlet diketuk
 */
@Composable
fun OutletItemCard(
    item: OutletRouteItem,
    onSelectOutlet: (Long) -> Unit
) {
    val outlet = item.outlet
    val onCardClick = remember(outlet.id, onSelectOutlet) { { onSelectOutlet(outlet.id) } }
    val statusColor = when (item.visitStatus) {
        OutletVisitStatus.BELUM_DISURVEI -> AswSecondaryText
        OutletVisitStatus.PENDING -> Color(0xFF92400E)
        OutletVisitStatus.TERSINKRON -> Color(0xFF065F46)
        OutletVisitStatus.PERLU_REVISIT -> Color(0xFF9A3412)
        OutletVisitStatus.GAGAL -> AswErrorRed
    }
    val statusBgColor = when (item.visitStatus) {
        OutletVisitStatus.BELUM_DISURVEI -> Color(0xFFE5E7EB)
        OutletVisitStatus.PENDING -> Color(0xFFFEF3C7)
        OutletVisitStatus.TERSINKRON -> Color(0xFFD1FAE5)
        OutletVisitStatus.PERLU_REVISIT -> Color(0xFFFFEDD5)
        OutletVisitStatus.GAGAL -> Color(0xFFFEE2E2)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape14)
            .background(AswSurfaceLight, shape = CardShape14)
            .border(1.dp, AswBorder, shape = CardShape14)
            .clickable(onClick = onCardClick)
            .padding(14.dp)
    ) {
            // Baris 1: Metadata Rute & Status Kunjungan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (outlet.stopOrder > 0) {
                        Box(
                            modifier = Modifier
                                .background(AswCharcoal, shape = TagShape6)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${outlet.stopOrder}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    val isWajib = outlet.outletType.equals("wajib", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isWajib) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                                shape = TagShape6
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isWajib) "WAJIB" else "TAMBAHAN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWajib) AswPrimaryRed else Color(0xFF0369A1)
                        )
                    }
                    val (pillarBg, pillarFg) = pillarBadgeColors(outlet.classOutlet)
                    Box(
                        modifier = Modifier
                            .background(pillarBg, shape = TagShape6)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = outlet.classOutlet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = pillarFg
                        )
                    }
                    if (outlet.isPareto && !outlet.classOutlet.contains("Pareto", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B5CF6), shape = TagShape6)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PARETO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .background(color = statusBgColor, shape = PillShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.visitStatus.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris 2: Identitas Toko dengan Ikon Terbingkai Rapi
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFF1F2), shape = CardShape10),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Store",
                        tint = AswPrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = outlet.namaToko,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AswCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = outlet.kodeToko,
                        fontSize = 11.sp,
                        color = AswSecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Baris 3: Alamat
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Alamat",
                    tint = AswSecondaryText,
                    modifier = Modifier
                        .size(15.dp)
                        .padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = outlet.alamat,
                    fontSize = 12.sp,
                    color = AswSecondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris 4: Panel Kolaborasi Pasangan Surveyor & Skor Gabungan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = CardShape8)
                    .border(1.dp, AswBorder, CardShape8)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Partner: ${outlet.partnerName ?: "-"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AswCharcoal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val leaderBadge = if (outlet.isVisitedLeader) "✓ Selesai" else "⏳ Menunggu"
                    val memberBadge = if (outlet.isVisitedMember) "✓ Selesai" else "⏳ Menunggu"
                    Text(
                        text = "Leader: $leaderBadge  •  Member: $memberBadge",
                        fontSize = 10.sp,
                        color = AswSecondaryText
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val isFinalScore = outlet.isSurveyComplete && outlet.finalCombinedScore != null
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isFinalScore) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                            shape = TagShape6
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFinalScore) {
                            "Skor: %.1f%%".format(outlet.finalCombinedScore)
                        } else {
                            "Parsial"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFinalScore) Color(0xFF065F46) else AswSecondaryText
                    )
                }
            }

            // Baris 5: Info Riwayat Survei & Peringatan Revisit
            if (outlet.hasSurveyedBefore) {
                Spacer(modifier = Modifier.height(6.dp))
                val lastScoreLabel = outlet.lastSurveyScore?.let { " (Skor: %.1f%%)".format(it) }.orEmpty()
                Text(
                    text = "📋 Pernah disurvei: ${outlet.lastSurveyDate ?: "-"}$lastScoreLabel",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9A3412)
                )
            }

            // Peringatan Revisit (Kontras Tajam & Tanpa Duplikasi Label)
            if (item.needsRevisit) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBEB), shape = CardShape8)
                        .border(1.dp, Color(0xFFFCD34D), CardShape8)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    val rawReason = item.revisitReason?.trim().orEmpty()
                    val cleanReason = rawReason.removePrefix("Revisit:").removePrefix("revisit:").trim()
                        .ifBlank { "Data belum lengkap" }
                    Text(
                        text = "⚠️ Revisit: $cleanReason",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            if (item.visitStatus == OutletVisitStatus.GAGAL && !item.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kendala: ${item.errorMessage}",
                    fontSize = 11.sp,
                    color = AswErrorRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Baris 6: Footer Status Rak & Petunjuk Buka Profil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (outlet.isSewaDisplay) Color(0xFFFFF1F2) else Color(0xFFF3F4F6),
                            shape = PillShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (outlet.isSewaDisplay) "★ Wajib Sewa Rak" else "Non Sewa Rak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (outlet.isSewaDisplay) AswPrimaryRed else AswSecondaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lihat Profil & Survei",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AswPrimaryRed
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Buka Profil",
                        tint = AswPrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
}

/**
 * Dialog profil lengkap toko yang muncul saat pengguna mengetuk kartu outlet di Dashboard.
 * Menampilkan identitas toko, kontak pemilik/PIC, alamat, koordinat master LA/LG, status sewa rak,
 * serta progres kolaborasi tim Leader & Member.
 *
 * @param item Gabungan data toko dan status survei hari ini
 * @param role Peran surveyor aktif ("leader" atau "member")
 * @param onDismiss Callback saat dialog ditutup
 * @param onStartSurvey Callback saat tombol mulai/edit survei ditekan dari dalam profil
 */
@Composable
fun OutletProfileDialog(
    item: OutletRouteItem,
    role: String,
    onDismiss: () -> Unit,
    onStartSurvey: () -> Unit
) {
    val context = LocalContext.current
    val outlet = item.outlet
    val isSynced = item.visitStatus == OutletVisitStatus.TERSINKRON
    val isWajib = outlet.outletType.equals("wajib", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFF1F2), shape = CardShape10),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Profil Toko",
                        tint = AswPrimaryRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = outlet.namaToko,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal
                    )
                    Text(
                        text = "${outlet.kodeToko}  •  Kode Harga: ${outlet.priceCode}",
                        fontSize = 11.sp,
                        color = AswSecondaryText
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tag Klasifikasi & Sewa Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (outlet.stopOrder > 0) {
                        Box(
                            modifier = Modifier
                                .background(AswCharcoal, shape = TagShape6)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Urutan #${outlet.stopOrder}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isWajib) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                                shape = TagShape6
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isWajib) "WAJIB" else "TAMBAHAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWajib) AswPrimaryRed else Color(0xFF0369A1)
                        )
                    }
                    val (pillarBg, pillarFg) = pillarBadgeColors(outlet.classOutlet)
                    Box(
                        modifier = Modifier
                            .background(pillarBg, shape = TagShape6)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = outlet.classOutlet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = pillarFg
                        )
                    }
                    if (outlet.isPareto && !outlet.classOutlet.contains("Pareto", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B5CF6), shape = TagShape6)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PARETO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (outlet.hasSurveyedBefore) {
                    val lastScoreLabel = outlet.lastSurveyScore?.let { " • Skor %.1f%%".format(it) }.orEmpty()
                    Text(
                        text = "📋 Riwayat Survei: ${outlet.lastSurveyDate ?: "-"}$lastScoreLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9A3412)
                    )
                }

                HorizontalDivider(color = AswBorder)

                ProfileDetailRow(
                    icon = Icons.Default.Person,
                    label = "Pemilik / PIC Toko",
                    value = outlet.ownerName?.takeIf { it.isNotBlank() } ?: "-"
                )
                ProfileDetailRow(
                    icon = Icons.Default.Phone,
                    label = "Nomor Telepon",
                    value = outlet.contactPhone?.takeIf { it.isNotBlank() } ?: "-"
                )
                ProfileDetailRow(
                    icon = Icons.Default.LocationOn,
                    label = "Alamat Lengkap",
                    value = outlet.alamat
                )

                // Kotak Koordinat Master & Sewa Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AswSurfaceLight, shape = CardShape10)
                        .border(1.dp, AswBorder, CardShape10)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "INFORMASI LOKASI & DISPLAY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswPrimaryRed
                    )
                    val coordText = if (outlet.latitude != null && outlet.longitude != null) {
                        "LA: %.7f  |  LG: %.7f".format(outlet.latitude, outlet.longitude)
                    } else {
                        "Koordinat master belum terdaftar"
                    }
                    Text(
                        text = coordText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AswCharcoal
                    )
                    Text(
                        text = if (outlet.isSewaDisplay) {
                            "Status Rak: ★ Wajib Sewa Rak Display ASW Foods"
                        } else {
                            "Status Rak: Non Sewa Rak"
                        },
                        fontSize = 11.sp,
                        color = AswSecondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { openGoogleMapsDirections(context, outlet.latitude, outlet.longitude, outlet.namaToko, outlet.alamat) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = PillShape,
                        border = BorderStroke(1.dp, AswCharcoal),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Open Direction",
                            tint = AswCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open Direction",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AswCharcoal
                        )
                    }
                }

                // Kotak Status Tim & Skor
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = CardShape10)
                        .border(1.dp, AswBorder, CardShape10)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "STATUS SURVEI TIM (${item.visitStatus.label.uppercase()})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal
                    )
                    Text(
                        text = "Partner Surveyor: ${outlet.partnerName ?: "-"}",
                        fontSize = 11.sp,
                        color = AswCharcoal
                    )
                    Text(
                        text = "Leader: ${if (outlet.isVisitedLeader) "✓ Selesai" else "⏳ Menunggu"}  •  " +
                            "Member: ${if (outlet.isVisitedMember) "✓ Selesai" else "⏳ Menunggu"}",
                        fontSize = 11.sp,
                        color = AswSecondaryText
                    )
                    val finalScoreText = if (outlet.isSurveyComplete && outlet.finalCombinedScore != null) {
                        "Skor Gabungan Final: %.1f%%".format(outlet.finalCombinedScore)
                    } else {
                        "Skor Gabungan: Parsial (menunggu Leader & Member lengkap)"
                    }
                    Text(
                        text = finalScoreText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (outlet.isSurveyComplete) AswSuccessGreen else AswSecondaryText
                    )
                }

                if (item.needsRevisit) {
                    val rawReason = item.revisitReason?.trim().orEmpty()
                    val cleanReason = rawReason.removePrefix("Revisit:").removePrefix("revisit:").trim()
                        .ifBlank { "Data belum lengkap" }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFBEB), shape = CardShape8)
                            .border(1.dp, Color(0xFFFCD34D), CardShape8)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Revisit: $cleanReason",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isSynced) {
                val actionLabel = when (item.visitStatus) {
                    OutletVisitStatus.PENDING, OutletVisitStatus.GAGAL -> "Edit Survei"
                    OutletVisitStatus.PERLU_REVISIT -> "Lakukan Revisit"
                    else -> if (role.lowercase() == "leader") "Wawancara (L1-L12)" else "Audit Fisik (M1-M4)"
                }
                Button(
                    onClick = onStartSurvey,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AswPrimaryRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = PillShape,
                border = BorderThin
            ) {
                Text(text = "Tutup", fontSize = 12.sp, color = AswCharcoal)
            }
        }
    )
}

@Composable
private fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AswPrimaryRed,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = AswSecondaryText
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AswCharcoal
            )
        }
    }
}

/**
 * Menentukan warna latar dan warna teks badge 5 Pilar `class_outlet` sesuai panduan UI v2.1.0.
 */
private fun pillarBadgeColors(classOutlet: String): Pair<Color, Color> {
    return when {
        classOutlet.equals("RWO", ignoreCase = true) -> Color(0xFFB2000C) to Color.White
        classOutlet.contains("Pareto", ignoreCase = true) -> Color(0xFF8B5CF6) to Color.White
        classOutlet.equals("NGVO", ignoreCase = true) -> Color(0xFF059669) to Color.White
        classOutlet.equals("Grooming", ignoreCase = true) -> Color(0xFFD97706) to Color.White
        classOutlet.equals("NOO", ignoreCase = true) -> Color(0xFF64748B) to Color.White
        else -> Color(0xFFE2E8F0) to AswCharcoal
    }
}

/**
 * Membuka aplikasi Google Maps langsung pada menu Direction (pratinjau rute dari lokasi saat ini)
 * menuju koordinat toko, atau fallback ke browser Google Maps Directions jika aplikasi tidak terpasang.
 */
private fun openGoogleMapsDirections(
    context: Context,
    latitude: Double?,
    longitude: Double?,
    namaToko: String,
    alamat: String
) {
    val destination = if (latitude != null && longitude != null) {
        "$latitude,$longitude"
    } else {
        Uri.encode("$namaToko, $alamat")
    }

    val directionsUri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&destination=$destination"
    )

    val mapsAppIntent = Intent(Intent.ACTION_VIEW, directionsUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(mapsAppIntent)
    } catch (_: ActivityNotFoundException) {
        val webMapsIntent = Intent(Intent.ACTION_VIEW, directionsUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(webMapsIntent) }
    }
}
