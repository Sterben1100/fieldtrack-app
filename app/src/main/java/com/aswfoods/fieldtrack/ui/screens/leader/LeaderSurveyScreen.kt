package com.aswfoods.fieldtrack.ui.screens.leader

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity
import com.aswfoods.fieldtrack.ui.components.IncompleteCheckoutDialog
import com.aswfoods.fieldtrack.ui.theme.AswBorder
import com.aswfoods.fieldtrack.ui.theme.AswCharcoal
import com.aswfoods.fieldtrack.ui.theme.AswErrorRed
import com.aswfoods.fieldtrack.ui.theme.AswPendingOrange
import com.aswfoods.fieldtrack.ui.theme.AswPrimaryRed
import com.aswfoods.fieldtrack.ui.theme.AswSecondaryText
import com.aswfoods.fieldtrack.ui.theme.AswSurfaceLight
import com.aswfoods.fieldtrack.util.AsyncPhotoPreview

private val OPTIONS_WITH_NA = listOf("Ya", "Tidak", "N/A")
private val OPTIONS_NO_NA = listOf("Ya", "Tidak")
private val LeaderCardShape = RoundedCornerShape(12.dp)
private val LeaderInnerShape = RoundedCornerShape(8.dp)
private val LeaderPillShape = RoundedCornerShape(percent = 50)
private val LeaderBorderNormal = BorderStroke(1.dp, AswBorder)
private val LeaderBorderError = BorderStroke(1.5.dp, AswErrorRed)

/**
 * Layar kuesioner wawancara pemilik toko untuk Surveyor Leader (L1 - L12)
 * lengkap dengan validasi L9, opsi L12 = N/A (Revisit L12), serta Modal Check-Out Dini.
 *
 * @param outletId ID toko yang sedang diwawancarai
 * @param viewModel ViewModel pengelola state dan validasi kuesioner Leader
 * @param onBackClick Callback untuk kembali ke layar Dashboard tanpa menyimpan
 * @param onSurveySaved Callback saat kuesioner valid dan berhasil disimpan ke Room lokal
 */
@Composable
fun LeaderSurveyScreen(
    outletId: Long,
    viewModel: LeaderSurveyViewModel,
    onBackClick: () -> Unit,
    onSurveySaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(outletId) {
        viewModel.initSurvey(outletId)
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
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearValidationError()
        }
    }

    if (state.showIncompleteModal) {
        IncompleteCheckoutDialog(
            roleLabel = "Leader",
            onDismiss = { viewModel.dismissIncompleteModal() },
            onConfirm = { reasonType, reasonText ->
                viewModel.submitIncompleteCheckout(reasonType, reasonText)
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    containerColor = AswErrorRed,
                    contentColor = Color.White,
                    shape = LeaderCardShape
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
                        text = "Kuesioner Pemilik Toko (Leader)",
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
                    shape = LeaderPillShape,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jawab Ya / Tidak / N/A sesuai kondisi terakhir.",
                    fontSize = 11.sp,
                    color = AswSecondaryText
                )
                Text(
                    text = "Terisi: ${state.answeredCount}/${state.questions.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.answeredCount == state.questions.size) AswPrimaryRed else AswCharcoal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scroll Column untuk 12 kartu tetap agar nol jank saat scroll naik-turun
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.questions.forEach { question ->
                    key(question.code) {
                        val l9Error = if (question.code == "L9") state.l9NoteError else null
                        val l12Error = if (question.code == "L12") state.picAbsenceError else null
                        LeaderQuestionCard(
                            question = question,
                            supervisors = if (question.code == "L12") state.supervisors else emptyList(),
                            selectedSupervisorId = if (question.code == "L12") state.selectedSupervisorId else null,
                            isL12Na = if (question.code == "L12") state.isL12Na else false,
                            picAbsenceReason = if (question.code == "L12") state.picAbsenceReason else "",
                            l9NoteError = l9Error,
                            picAbsenceError = l12Error,
                            onAnswerSelect = { ans -> viewModel.updateAnswer(question.code, ans) },
                            onNotesChange = { notes -> viewModel.updateNotes(question.code, notes) },
                            onRatingChange = { rating -> viewModel.updateRating(rating) },
                            onSelectSupervisor = { spvId -> viewModel.selectSupervisor(spvId) },
                            onSelectL12Na = { viewModel.selectL12Na() },
                            onPicAbsenceReasonChange = { reason -> viewModel.updatePicAbsenceReason(reason) }
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
                shape = LeaderPillShape,
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
 * Kartu terisolasi untuk satu butir pertanyaan kuesioner Leader agar hanya kartu yang berubah
 * yang mengalami recomposition saat interaksi pengguna.
 */
@Composable
private fun LeaderQuestionCard(
    question: QuestionItem,
    supervisors: List<SupervisorEntity>,
    selectedSupervisorId: Long?,
    isL12Na: Boolean,
    picAbsenceReason: String,
    l9NoteError: String?,
    picAbsenceError: String?,
    onAnswerSelect: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onSelectSupervisor: (Long) -> Unit,
    onSelectL12Na: () -> Unit,
    onPicAbsenceReasonChange: (String) -> Unit
) {
    val hasError = l9NoteError != null || picAbsenceError != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (hasError) Color(0xFFFEF2F2) else AswSurfaceLight,
                shape = LeaderCardShape
            )
            .border(
                width = 1.dp,
                color = if (hasError) AswErrorRed else AswBorder,
                shape = LeaderCardShape
            )
            .padding(14.dp)
    ) {
            Text(
                text = "${question.code}. ${question.aspect}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AswPrimaryRed
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = question.questionText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AswCharcoal
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                question.isRatingType -> {
                    val ratingLabel = if (question.rating != null) {
                        "Nilai Kepuasan: ${question.rating} / 10"
                    } else {
                        "Belum dinilai (Geser slider 0 - 10)"
                    }
                    Text(
                        text = ratingLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (question.rating != null) AswPrimaryRed else AswSecondaryText
                    )
                    Slider(
                        value = (question.rating ?: 5).toFloat(),
                        onValueChange = { onRatingChange(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = if (question.rating != null) AswPrimaryRed else AswSecondaryText,
                            activeTrackColor = if (question.rating != null) AswPrimaryRed else AswBorder
                        )
                    )
                }

                question.isPhotoChoiceType -> {
                    Text(
                        text = "Pilih SPV yang dikenali oleh toko:",
                        fontSize = 12.sp,
                        color = AswSecondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (supervisors.isEmpty()) {
                        Text(
                            text = "Daftar SPV belum tersedia. Unduh ulang rute di Dashboard.",
                            fontSize = 11.sp,
                            color = AswSecondaryText
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            supervisors.forEach { spv ->
                                val isSelected = !isL12Na && selectedSupervisorId == spv.id
                                val initials = remember(spv.id, spv.name) {
                                    spv.name
                                        .split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .joinToString("") { it.first().uppercaseChar().toString() }
                                        .ifBlank { "SPV" }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) AswPrimaryRed else AswBorder,
                                            shape = LeaderInnerShape
                                        )
                                        .background(
                                            if (isSelected) Color(0xFFFEE2E2) else Color.White,
                                            shape = LeaderInnerShape
                                        )
                                        .clickable { onSelectSupervisor(spv.id) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AsyncPhotoPreview(
                                            pathOrUrl = spv.photoUrl,
                                            fallbackLabel = initials,
                                            contentDescription = "Foto ${spv.name}",
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, AswBorder, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = spv.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = AswCharcoal,
                                            textAlign = TextAlign.Center
                                        )
                                        val spvCode = spv.salesCode.ifBlank { spv.nip }
                                        if (spvCode.isNotBlank()) {
                                            Text(
                                                text = spvCode,
                                                fontSize = 9.sp,
                                                color = AswSecondaryText,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isL12Na) 1.5.dp else 1.dp,
                                color = if (isL12Na) AswPendingOrange else AswBorder,
                                shape = LeaderInnerShape
                            )
                            .background(
                                color = if (isL12Na) Color(0xFFFFFBEB) else Color.White,
                                shape = LeaderInnerShape
                            )
                            .clickable { onSelectL12Na() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "N/A — Pemilik / PIC Tidak di Tempat (Jadwalkan Revisit L12)",
                            fontSize = 11.sp,
                            fontWeight = if (isL12Na) FontWeight.Bold else FontWeight.Medium,
                            color = if (isL12Na) AswCharcoal else AswSecondaryText
                        )
                    }

                    if (isL12Na) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = picAbsenceReason,
                            onValueChange = onPicAbsenceReasonChange,
                            label = { Text("Alasan PIC Tidak di Tempat (Wajib)", fontSize = 11.sp) },
                            placeholder = {
                                Text("Contoh: Pemilik keluar kota, toko dijaga kasir pengganti", fontSize = 11.sp)
                            },
                            isError = picAbsenceError != null,
                            supportingText = picAbsenceError?.let { err ->
                                {
                                    Text(text = err, color = AswErrorRed, fontSize = 11.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AswPrimaryRed,
                                focusedLabelColor = AswPrimaryRed,
                                errorBorderColor = AswErrorRed,
                                errorLabelColor = AswErrorRed
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = LeaderInnerShape,
                            minLines = 2,
                            maxLines = 3
                        )
                    }
                }

                else -> {
                    val options = if (question.allowNa) OPTIONS_WITH_NA else OPTIONS_NO_NA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { option ->
                            val isSelected = question.selectedAnswer?.equals(option, ignoreCase = true) == true
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) AswPrimaryRed else Color.White,
                                        shape = LeaderPillShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AswPrimaryRed else AswBorder,
                                        shape = LeaderPillShape
                                    )
                                    .clickable { onAnswerSelect(option) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else AswCharcoal
                                )
                            }
                        }
                    }

                    if (question.code == "L9" || question.isTextNotesType) {
                        val isL9Required = question.code == "L9" &&
                            question.selectedAnswer?.equals("Ya", ignoreCase = true) == true
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question.notes,
                            onValueChange = onNotesChange,
                            label = {
                                Text(
                                    text = when {
                                        isL9Required -> "Catatan Reward Belum Cair (Wajib)"
                                        question.isTextNotesType -> "Catatan / Masukan Toko"
                                        else -> "Catatan Tambahan"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            placeholder = {
                                Text(
                                    text = when {
                                        isL9Required -> "Sebutkan nama/periode program reward..."
                                        question.isTextNotesType -> "Tulis masukan atau saran dari pemilik toko..."
                                        else -> "Tulis catatan tambahan..."
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            isError = l9NoteError != null,
                            supportingText = l9NoteError?.let { err ->
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
                            shape = LeaderInnerShape,
                            singleLine = false,
                            maxLines = 3
                        )
                    }
                }
            }
    }
}
