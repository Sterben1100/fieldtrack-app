package com.aswfoods.fieldtrack.ui.screens.member

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aswfoods.fieldtrack.ui.components.IncompleteCheckoutDialog
import com.aswfoods.fieldtrack.ui.theme.AswBorder
import com.aswfoods.fieldtrack.ui.theme.AswCharcoal
import com.aswfoods.fieldtrack.ui.theme.AswErrorRed
import com.aswfoods.fieldtrack.ui.theme.AswPendingOrange
import com.aswfoods.fieldtrack.ui.theme.AswPrimaryRed
import com.aswfoods.fieldtrack.ui.theme.AswSecondaryText
import com.aswfoods.fieldtrack.ui.theme.AswSuccessGreen
import com.aswfoods.fieldtrack.ui.theme.AswSurfaceLight
import com.aswfoods.fieldtrack.util.AsyncPhotoPreview
import com.aswfoods.fieldtrack.util.ImageHelper
import com.aswfoods.fieldtrack.util.LocationHelper
import kotlinx.coroutines.launch

private val RAK_OPTIONS = listOf(
    "compliant" to "Terpasang & Sesuai Planogram (100%)",
    "items_5_non_compliant" to "5 Item Terpajang, Tidak Sesuai (75%)",
    "items_3_non_compliant" to "3 Item Terpajang, Tidak Sesuai (50%)",
    "items_2_non_compliant" to "2 Item Terpajang, Tidak Sesuai (25%)",
    "no_items" to "Tidak Ada Produk Terpajang (0%)"
)
private val MemberCardShape12 = RoundedCornerShape(12.dp)
private val MemberCardShape10 = RoundedCornerShape(10.dp)
private val MemberBoxShape8 = RoundedCornerShape(8.dp)
private val MemberTagShape6 = RoundedCornerShape(6.dp)
private val MemberPillShape = RoundedCornerShape(percent = 50)
private val MemberBorderThin = BorderStroke(1.dp, AswBorder)
private val MemberBorderRed = BorderStroke(1.dp, AswPrimaryRed)
private val NumberKeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

/**
 * Layar formulir Audit Fisik Lapangan untuk Surveyor Member (Kriteria M1 - M4)
 * dengan dukungan GPS nyata (radius max 15m + alamat & LA/LG), kalkulator skor proporsional,
 * foto kamera terkompresi, Modal Check-Out Dini, dan scroll ringan bebas jank.
 *
 * @param outletId ID toko target audit
 * @param viewModel ViewModel pengelola state audit fisik Member
 * @param onBackClick Callback untuk kembali ke Dashboard tanpa menyimpan
 * @param onSurveySaved Callback saat hasil audit berhasil disimpan ke Room lokal
 */
@Composable
fun MemberSurveyScreen(
    outletId: Long,
    viewModel: MemberSurveyViewModel,
    onBackClick: () -> Unit,
    onSurveySaved: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }
    var pendingSkuProductId by remember { mutableStateOf<Long?>(null) }
    var pendingCameraAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isSnackbarSuccess by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val path = pendingPhotoPath
        val skuId = pendingSkuProductId
        if (success && !path.isNullOrBlank()) {
            if (skuId == null) {
                viewModel.onRakPhotoCaptured(path)
            } else {
                viewModel.onSkuPhotoCaptured(skuId, path)
            }
        }
        pendingPhotoPath = null
        pendingSkuProductId = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCameraAction?.invoke()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Izin kamera diperlukan untuk mengambil foto bukti.")
            }
        }
        pendingCameraAction = null
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshGpsLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Izin lokasi ditolak. Geotag tidak dapat diperbarui.")
            }
        }
    }

    val launchCamera: (Long?) -> Unit = remember(outletId) {
        { skuId ->
            val action = {
                val prefix = if (skuId == null) "rak_${outletId}" else "sku_${outletId}_$skuId"
                val file = ImageHelper.createPhotoFile(context, prefix)
                pendingPhotoPath = file.absolutePath
                pendingSkuProductId = skuId
                val uri = ImageHelper.getUriForFile(context, file)
                takePictureLauncher.launch(uri)
            }

            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
                action()
            } else {
                pendingCameraAction = action
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val onRefreshOrRequestGps = remember(context) {
        {
            if (LocationHelper.hasLocationPermission(context)) {
                viewModel.refreshGpsLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LaunchedEffect(outletId) {
        viewModel.initSurvey(outletId)
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSavedEvent()
            onSurveySaved()
        }
    }

    LaunchedEffect(state.validationError) {
        val error = state.validationError
        if (!error.isNullOrBlank()) {
            isSnackbarSuccess = false
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearValidationError()
        }
    }

    LaunchedEffect(state.infoMessage) {
        val info = state.infoMessage
        if (!info.isNullOrBlank()) {
            isSnackbarSuccess = true
            snackbarHostState.showSnackbar(
                message = info,
                duration = SnackbarDuration.Short
            )
            viewModel.clearInfoMessage()
        }
    }

    if (state.showIncompleteModal) {
        IncompleteCheckoutDialog(
            roleLabel = "Member",
            onDismiss = { viewModel.dismissIncompleteModal() },
            onConfirm = { reasonType, reasonText ->
                viewModel.submitIncompleteCheckout(reasonType, reasonText)
            }
        )
    }

    if (state.showUpdateOutletPointDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateOutletPointDialog() },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Peringatan",
                        tint = AswPendingOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Peringatan Perbarui Titik Toko",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jika Anda ingin memperbarui titik ${state.outletName}, harap pastikan Anda berdiri tepat di titik toko berada saat ini.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AswErrorRed
                    )
                    Text(
                        text = "Aplikasi akan mengambil koordinat GPS posisi Anda sekarang untuk dijadikan titik lokasi toko terbaru. Jangan melakukan pembaruan titik di tempat yang tidak seharusnya atau di luar lokasi toko.",
                        fontSize = 12.sp,
                        color = AswCharcoal
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            viewModel.confirmUpdateOutletPoint()
                        } else {
                            viewModel.dismissUpdateOutletPointDialog()
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    shape = MemberPillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AswPrimaryRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Ya, Perbarui Titik",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissUpdateOutletPointDialog() },
                    shape = MemberPillShape,
                    border = MemberBorderThin
                ) {
                    Text(text = "Batal", fontSize = 12.sp, color = AswCharcoal)
                }
            }
        )
    }

    if (state.showOutOfRadiusDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOutOfRadiusDialog() },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Di Luar Radius Maksimal 15 Meter",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AswErrorRed
                )
            },
            text = {
                val distLabel = state.distanceMeters?.let { "%.1f meter".format(it) } ?: "-"
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Jarak GPS Anda saat ini $distLabel dari koordinat toko " +
                            "(batas radius pengambilan titik maksimal 15 meter). " +
                            "Anda dapat memperbarui GPS atau memperbarui titik toko jika koordinat toko belum sesuai.",
                        fontSize = 13.sp,
                        color = AswCharcoal
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.dismissOutOfRadiusDialog()
                            viewModel.openUpdateOutletPointDialog()
                        },
                        shape = MemberPillShape,
                        border = MemberBorderRed,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLocationAlt,
                            contentDescription = "Perbarui Titik Toko",
                            tint = AswPrimaryRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Perbarui Titik Toko",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AswPrimaryRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissOutOfRadiusDialog()
                        viewModel.refreshGpsLocation()
                    },
                    shape = MemberPillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AswPrimaryRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Perbarui GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.confirmOutOfRadiusCheckout() },
                    shape = MemberPillShape,
                    border = MemberBorderThin
                ) {
                    Text(text = "Tetap Simpan", fontSize = 12.sp, color = AswCharcoal)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    containerColor = if (isSnackbarSuccess) AswSuccessGreen else AswErrorRed,
                    contentColor = Color.White,
                    shape = MemberCardShape12
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
                .padding(16.dp)
        ) {
            // Header Toko + Tombol Kembali + Tombol Terkendala
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = AswCharcoal
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Audit Fisik Lapangan (Member)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal
                    )
                    Text(
                        text = state.outletName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AswPrimaryRed
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.openIncompleteModal() },
                    shape = MemberPillShape,
                    border = BorderStroke(1.dp, AswPendingOrange),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Terkendala",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AswPendingOrange
                    )
                }
            }
            Text(
                text = "Kategori: ${state.classOutlet} | ${if (state.isSewaDisplay) "Wajib Sewa Display" else "Non Sewa Display"}",
                fontSize = 11.sp,
                color = AswSecondaryText,
                modifier = Modifier.padding(start = 12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Kartu Ringkasan Skor Proporsional M1-M4 (Bab 2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MemberCardShape12,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                border = BorderStroke(1.dp, AswPrimaryRed.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Estimasi Skor Audit Member",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AswPrimaryRed
                        )
                        val m2Label = if (state.isRakApplicable) "M2:${state.rakScorePercent}%" else "M2:N/A"
                        val m3Label = if (state.isPriceTagApplicable) "M3:${state.priceTagScorePercent.toInt()}%" else "M3:N/A"
                        Text(
                            text = "M1:${state.geotagScorePercent}% | $m2Label | $m3Label | M4:${state.priceRangeScorePercent.toInt()}%",
                            fontSize = 11.sp,
                            color = AswCharcoal
                        )
                    }
                    Text(
                        text = "%.1f%%".format(state.estimatedMemberScore),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AswPrimaryRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scroll Column untuk M1 + M2 + 6 SKU tetap agar bebas jank saat scroll naik-turun
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MemberGeotagCard(
                    currentLat = state.currentLat,
                    currentLng = state.currentLng,
                    outletLat = state.outletLat,
                    outletLng = state.outletLng,
                    distanceMeters = state.distanceMeters,
                    geotagScorePercent = state.geotagScorePercent,
                    gpsAccuracyMeters = state.gpsAccuracyMeters,
                    isWithinRadius = state.isWithinRadius,
                    gpsStatusMessage = state.gpsStatusMessage,
                    gpsAddress = state.gpsAddress,
                    isResolvingAddress = state.isResolvingAddress,
                    isFetchingGps = state.isFetchingGps,
                    isUpdatingOutletPoint = state.isUpdatingOutletPoint,
                    isDemoMode = state.isDemoMode,
                    onRefreshGps = onRefreshOrRequestGps,
                    onRequestUpdateOutletPoint = { viewModel.openUpdateOutletPointDialog() },
                    onSimulateDemoGps = { viewModel.simulateAtOutletLocation() }
                )

                MemberRakCard(
                    isRakApplicable = state.isRakApplicable,
                    selectedRakCondition = state.selectedRakCondition,
                    rakPhotoPath = state.rakPhotoPath,
                    onSelectCondition = { viewModel.updateRakCondition(it) },
                    onCaptureRakPhoto = { launchCamera(null) }
                )

                Text(
                    text = if (state.isPriceTagApplicable) {
                        "M3 & M4: AUDIT 6 SKU FOKUS (TOP 5 + 1 NPL)"
                    } else {
                        "M4: AUDIT HARGA 6 SKU (PRICE TAG N/A UNTUK TRADER)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AswPrimaryRed
                )

                state.skuAudits.forEach { item ->
                    key(item.product.id) {
                        MemberSkuAuditCard(
                            item = item,
                            isPriceTagApplicable = state.isPriceTagApplicable,
                            onPriceChange = { price -> viewModel.updateSkuPrice(item.product.id, price) },
                            onToggleHasPriceTag = { viewModel.toggleHasPriceTag(item.product.id) },
                            onTogglePriceTagCompliant = { viewModel.togglePriceTagCompliant(item.product.id) },
                            onCaptureSkuPhoto = { launchCamera(item.product.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol CTA Check-Out
            Button(
                onClick = { viewModel.requestCheckout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MemberPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AswPrimaryRed,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Simpan & Check-Out Toko",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Kartu Kriteria M1 terisolasi untuk menampilkan jarak, status radius 15m, koordinat LA/LG, dan alamat GPS.
 */
@Composable
private fun MemberGeotagCard(
    currentLat: Double?,
    currentLng: Double?,
    outletLat: Double?,
    outletLng: Double?,
    distanceMeters: Double?,
    geotagScorePercent: Int,
    gpsAccuracyMeters: Float?,
    isWithinRadius: Boolean,
    gpsStatusMessage: String,
    gpsAddress: String?,
    isResolvingAddress: Boolean,
    isFetchingGps: Boolean,
    isUpdatingOutletPoint: Boolean,
    isDemoMode: Boolean,
    onRefreshGps: () -> Unit,
    onRequestUpdateOutletPoint: () -> Unit,
    onSimulateDemoGps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AswSurfaceLight, shape = MemberCardShape12)
            .border(1.dp, AswBorder, shape = MemberCardShape12)
            .padding(14.dp)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "M1: KEAKURATAN GEOTAG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AswPrimaryRed
                )
                OutlinedButton(
                    onClick = onRefreshGps,
                    shape = MemberPillShape,
                    border = MemberBorderThin,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    enabled = !isFetchingGps
                ) {
                    if (isFetchingGps) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = AswPrimaryRed
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Perbarui GPS",
                            tint = AswCharcoal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Perbarui GPS",
                            fontSize = 11.sp,
                            color = AswCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val distanceText = when {
                        currentLat == null || currentLng == null -> "Titik GPS belum terkunci"
                        distanceMeters == null -> "Lat: %.5f, Lng: %.5f".format(currentLat, currentLng)
                        else -> "Selisih Jarak: %.1f meter".format(distanceMeters)
                    }
                    Text(
                        text = distanceText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AswCharcoal
                    )
                    Text(
                        text = gpsStatusMessage,
                        fontSize = 11.sp,
                        color = AswSecondaryText
                    )
                    if (currentLat != null && currentLng != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = MemberBoxShape8)
                                .border(1.dp, AswBorder, MemberBoxShape8)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "LA: %.7f  |  LG: %.7f".format(currentLat, currentLng),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AswCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val addressLine = when {
                                isResolvingAddress -> "Mengambil alamat lokasi GPS..."
                                !gpsAddress.isNullOrBlank() -> gpsAddress
                                else -> "Alamat belum tersedia"
                            }
                            Text(
                                text = "Alamat GPS: $addressLine",
                                fontSize = 10.sp,
                                color = AswSecondaryText,
                                maxLines = 2
                            )
                            if (gpsAccuracyMeters != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                val isAccurate = gpsAccuracyMeters <= LocationHelper.MAX_ACCURACY_METERS
                                Text(
                                    text = "Akurasi GPS HP: ±%.1f meter (%s)".format(
                                        gpsAccuracyMeters,
                                        if (isAccurate) "Presisi ≤ 15 m" else "Batas ideal ≤ 15 m"
                                    ),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAccurate) AswSuccessGreen else Color(0xFF92400E)
                                )
                            }
                            if (distanceMeters != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isWithinRadius) {
                                        "✓ Dalam radius pengambilan titik (≤ 15 m)"
                                    } else {
                                        "✕ Di luar radius maksimal 15 meter"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWithinRadius) AswSuccessGreen else AswErrorRed
                                )
                            }
                        }
                    }
                    val needsOutletPointUpdate = (outletLat == null || outletLng == null) ||
                        (distanceMeters != null && !isWithinRadius)
                    if (needsOutletPointUpdate) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRequestUpdateOutletPoint,
                            enabled = !isUpdatingOutletPoint && !isFetchingGps,
                            shape = MemberPillShape,
                            border = MemberBorderRed,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (isUpdatingOutletPoint) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = AswPrimaryRed
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Memperbarui Titik Toko...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AswPrimaryRed
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddLocationAlt,
                                    contentDescription = "Perbarui Titik Toko",
                                    tint = AswPrimaryRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Perbarui Titik Toko",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AswPrimaryRed
                                )
                            }
                        }
                    }
                    if (isDemoMode && outletLat != null && outletLng != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onSimulateDemoGps,
                            shape = MemberPillShape,
                            border = BorderStroke(1.dp, AswPendingOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "Simulasi Titik Toko (Demo)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                currentLat == null -> AswSecondaryText
                                isWithinRadius -> AswSuccessGreen
                                geotagScorePercent >= 75 -> AswPendingOrange
                                else -> AswPrimaryRed
                            },
                            shape = MemberPillShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (distanceMeters != null) {
                            "Skor: $geotagScorePercent%"
                        } else {
                            "GPS"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
    }
}

/**
 * Kartu Kriteria M2 terisolasi untuk pemilihan kondisi rak sewa display dan foto bukti rak.
 */
@Composable
private fun MemberRakCard(
    isRakApplicable: Boolean,
    selectedRakCondition: String?,
    rakPhotoPath: String?,
    onSelectCondition: (String) -> Unit,
    onCaptureRakPhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AswSurfaceLight, shape = MemberCardShape12)
            .border(1.dp, AswBorder, shape = MemberCardShape12)
            .padding(14.dp)
    ) {
            Text(
                text = "M2: RAK DISPLAY TERPASANG",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AswPrimaryRed
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (!isRakApplicable) {
                Text(
                    text = "Kriteria N/A untuk tipe outlet ini (Bobot dialihkan proporsional secara otomatis)",
                    fontSize = 12.sp,
                    color = AswSecondaryText,
                    fontStyle = FontStyle.Italic
                )
            } else {
                Text(
                    text = "Pilih kondisi kepatuhan rak fisik:",
                    fontSize = 12.sp,
                    color = AswCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))

                RAK_OPTIONS.forEach { (key, label) ->
                    val isSelected = selectedRakCondition == key ||
                        (key == "compliant" && selectedRakCondition == "terpasang_rapih")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectCondition(key) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) AswPrimaryRed else AswBorder,
                                    shape = MemberPillShape
                                )
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) AswPrimaryRed else Color.Transparent,
                                        shape = MemberPillShape
                                    )
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = if (isSelected) AswPrimaryRed else AswCharcoal,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCaptureRakPhoto,
                        shape = MemberPillShape,
                        border = MemberBorderRed
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Foto Rak",
                            tint = AswPrimaryRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (rakPhotoPath == null) "Ambil Foto Rak" else "Foto Ulang Rak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AswPrimaryRed
                        )
                    }

                    if (!rakPhotoPath.isNullOrBlank()) {
                        AsyncPhotoPreview(
                            pathOrUrl = rakPhotoPath,
                            fallbackLabel = "RAK",
                            contentDescription = "Pratinjau Foto Rak",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(MemberBoxShape8)
                                .border(1.dp, AswBorder, MemberBoxShape8)
                        )
                    }
                }
            }
    }
}

/**
 * Kartu terisolasi untuk satu baris audit SKU fokus (M3 Price Tag & M4 Harga GM)
 * sehingga perubahan input hanya me-recompose kartu SKU terkait.
 */
@Composable
private fun MemberSkuAuditCard(
    item: SkuAuditItem,
    isPriceTagApplicable: Boolean,
    onPriceChange: (String) -> Unit,
    onToggleHasPriceTag: () -> Unit,
    onTogglePriceTagCompliant: () -> Unit,
    onCaptureSkuPhoto: () -> Unit
) {
    val hasPriceInput = (item.actualPriceInput.toDoubleOrNull() ?: 0.0) > 0.0
    val rangeText = remember(item.product.id, item.product.minPrice, item.product.maxPrice) {
        "Range GM: Rp %,d - Rp %,d".format(
            item.product.minPrice.toInt(),
            item.product.maxPrice.toInt()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AswSurfaceLight, shape = MemberCardShape10)
            .border(1.dp, AswBorder, shape = MemberCardShape10)
            .padding(12.dp)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.product.skuName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AswCharcoal,
                    modifier = Modifier.weight(1f)
                )
                if (item.product.isNpl) {
                    Box(
                        modifier = Modifier
                            .background(AswPrimaryRed, shape = MemberPillShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NPL",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = rangeText,
                fontSize = 11.sp,
                color = AswSecondaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.actualPriceInput,
                    onValueChange = onPriceChange,
                    label = { Text("Harga Aktual Toko (Rp)", fontSize = 10.sp) },
                    placeholder = { Text("Contoh: 8500", fontSize = 11.sp) },
                    keyboardOptions = NumberKeyboardOptions,
                    modifier = Modifier.weight(1f),
                    shape = MemberBoxShape8,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                !hasPriceInput -> AswSecondaryText
                                item.isPriceInRange -> AswSuccessGreen
                                else -> AswPrimaryRed
                            },
                            shape = MemberTagShape6
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            !hasPriceInput -> "Belum Diisi"
                            item.isPriceInRange -> "✓ Sesuai Range"
                            else -> "✕ Di Luar Range"
                        },
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isPriceTagApplicable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleHasPriceTag),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.hasPriceTag,
                        onCheckedChange = { onToggleHasPriceTag() },
                        colors = CheckboxDefaults.colors(checkedColor = AswPrimaryRed)
                    )
                    Text(
                        text = "Price tag tersedia di rak",
                        fontSize = 11.sp,
                        color = AswCharcoal
                    )
                }

                if (item.hasPriceTag) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onTogglePriceTagCompliant),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isPriceTagCompliant,
                            onCheckedChange = { onTogglePriceTagCompliant() },
                            colors = CheckboxDefaults.colors(checkedColor = AswPrimaryRed)
                        )
                        Text(
                            text = "Posisi & harga price tag sesuai planogram",
                            fontSize = 11.sp,
                            color = AswCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCaptureSkuPhoto,
                    shape = MemberPillShape,
                    border = MemberBorderThin,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Foto SKU",
                        tint = AswCharcoal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.skuPhotoPath == null) "Foto SKU" else "Foto Ulang SKU",
                        fontSize = 11.sp,
                        color = AswCharcoal
                    )
                }

                if (!item.skuPhotoPath.isNullOrBlank()) {
                    AsyncPhotoPreview(
                        pathOrUrl = item.skuPhotoPath,
                        fallbackLabel = "SKU",
                        contentDescription = "Pratinjau Foto ${item.product.skuName}",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MemberTagShape6)
                            .border(1.dp, AswBorder, MemberTagShape6)
                    )
                }
            }
    }
}
