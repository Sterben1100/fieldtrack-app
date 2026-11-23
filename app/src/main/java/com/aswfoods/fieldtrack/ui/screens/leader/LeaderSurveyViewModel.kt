package com.aswfoods.fieldtrack.ui.screens.leader

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.data.local.database.AppDatabase
import com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.data.remote.dto.LeaderQuestionResponseDto
import com.aswfoods.fieldtrack.data.remote.dto.LeaderSurveyRequest
import com.aswfoods.fieldtrack.data.repository.SurveyRepository
import com.aswfoods.fieldtrack.util.LocationHelper
import com.aswfoods.fieldtrack.util.LocationResult
import com.aswfoods.fieldtrack.worker.SyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Representasi satu butir kuesioner wawancara Leader (L1-L12).
 * Nilai awal `selectedAnswer` dan `rating` adalah null agar sistem dapat mendeteksi
 * survei belum lengkap saat surveyor menekan Check-Out.
 */
@Immutable
data class QuestionItem(
    val code: String,
    val aspect: String,
    val questionText: String,
    val answerType: String = "yes_no_na",
    val allowNa: Boolean = true,
    val selectedAnswer: String? = null, // "Ya", "Tidak", "N/A", atau null jika belum dijawab
    val notes: String = "",
    val rating: Int? = null
) {
    /** Apakah butir pertanyaan menggunakan tipe penilaian rating 0-10. */
    val isRatingType: Boolean
        get() = answerType.equals("rating", ignoreCase = true) ||
            answerType.equals("rating_0_10", ignoreCase = true) ||
            code.equals("L11", ignoreCase = true)

    /** Apakah butir pertanyaan menggunakan uji buta pilih foto SPV (L12). */
    val isPhotoChoiceType: Boolean
        get() = answerType.equals("photo_choice", ignoreCase = true) ||
            code.equals("L12", ignoreCase = true)

    /** Apakah butir pertanyaan merupakan masukan/catatan teks toko (L10). */
    val isTextNotesType: Boolean
        get() = answerType.equals("text_notes", ignoreCase = true) ||
            code.equals("L10", ignoreCase = true)
}

/**
 * State UI untuk layar kuesioner wawancara Leader.
 */
@Immutable
data class LeaderSurveyUiState(
    val outletId: Long = 0,
    val outletName: String = "",
    val existingSyncId: String? = null,
    val checkinLatitude: Double? = null,
    val checkinLongitude: Double? = null,
    val checkinAt: String? = null,
    val questions: List<QuestionItem> = emptyList(),
    val supervisors: List<SupervisorEntity> = emptyList(),
    val selectedSupervisorId: Long? = null,
    val isL12Na: Boolean = false,
    val picAbsenceReason: String = "",
    val picAbsenceError: String? = null,
    val l9NoteError: String? = null,
    val validationError: String? = null,
    val showIncompleteModal: Boolean = false,
    val isSaved: Boolean = false
) {
    /**
     * Menghitung jumlah butir pertanyaan yang telah memiliki jawaban terisi.
     */
    val answeredCount: Int
        get() = questions.count { q ->
            when {
                q.isRatingType -> q.rating != null
                q.isPhotoChoiceType -> selectedSupervisorId != null || isL12Na
                q.isTextNotesType -> !q.selectedAnswer.isNullOrBlank() || q.notes.isNotBlank()
                else -> !q.selectedAnswer.isNullOrBlank()
            }
        }
}

/**
 * ViewModel pengelola kuesioner wawancara pemilik toko (Leader L1-L12) secara offline-first,
 * termasuk penanganan L12 = N/A (Revisit L12) dan Modal Intersepsi Check-Out Dini (Bab 5).
 */
@Stable
class LeaderSurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val sessionManager = SessionManager(application)
    private val surveyRepository = SurveyRepository(RetrofitClient.apiService, database.pendingVisitDao())
    private val gson = Gson()

    private val _uiState = MutableStateFlow(LeaderSurveyUiState())
    val uiState: StateFlow<LeaderSurveyUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail toko, master pertanyaan L1-L12 (dari Room dengan fallback baku),
     * daftar SPV, serta jawaban draft/pending hari ini jika ada.
     *
     * @param outletId ID toko target wawancara
     */
    fun initSurvey(outletId: Long) {
        viewModelScope.launch {
            val outlet = database.outletDao().getOutletById(outletId)
            val spvList = database.supervisorDao().getAllSupervisors()
            val dbQuestions = database.surveyQuestionDao().getQuestionsByRole("leader")

            val baseQuestions = if (dbQuestions.isNotEmpty()) {
                dbQuestions.map { q ->
                    QuestionItem(
                        code = q.questionCode,
                        aspect = q.aspect,
                        questionText = q.questionText,
                        answerType = q.answerType,
                        allowNa = !q.questionCode.equals("L1", ignoreCase = true)
                    )
                }
            } else {
                defaultLeaderQuestions()
            }

            val today = todayDateString()
            val now = nowTimestampString()
            val existingVisit = surveyRepository.findTodayVisit(outletId, "leader", today)

            var hydratedQuestions = baseQuestions
            var selectedSpvId: Long? = null
            var isL12Na = false
            var picAbsenceReason = ""
            var existingSyncId: String? = null
            var checkinAt = now

            // Muat kembali jawaban apabila hari ini sudah pernah disimpan secara lokal
            if (existingVisit != null) {
                existingSyncId = existingVisit.offlineSyncId
                checkinAt = existingVisit.checkinAt ?: now
                val savedRequest = runCatching {
                    gson.fromJson(existingVisit.payloadJson, LeaderSurveyRequest::class.java)
                }.getOrNull()

                if (savedRequest != null) {
                    picAbsenceReason = savedRequest.picAbsenceReason.orEmpty()
                    val savedMap = savedRequest.responses.associateBy { it.questionCode }
                    hydratedQuestions = baseQuestions.map { q ->
                        val prev = savedMap[q.code]
                        if (prev != null) {
                            when {
                                q.isRatingType -> q.copy(
                                    selectedAnswer = null,
                                    notes = prev.notes ?: "",
                                    rating = prev.ratingValue ?: prev.answerValue.toIntOrNull()
                                )
                                q.isTextNotesType -> {
                                    val isChoice = prev.answerValue in listOf("Ya", "Tidak", "N/A")
                                    q.copy(
                                        selectedAnswer = if (isChoice) prev.answerValue else null,
                                        notes = prev.notes?.takeIf { it.isNotBlank() }
                                            ?: if (!isChoice) prev.answerValue else ""
                                    )
                                }
                                else -> q.copy(
                                    selectedAnswer = prev.answerValue,
                                    notes = prev.notes ?: "",
                                    rating = prev.ratingValue
                                )
                            }
                        } else {
                            q
                        }
                    }
                    val l12Prev = savedMap["L12"]
                    if (l12Prev != null) {
                        if (l12Prev.answerValue.equals("N/A", ignoreCase = true)) {
                            isL12Na = true
                            selectedSpvId = null
                        } else {
                            selectedSpvId = l12Prev.selectedSupervisorId
                        }
                    }
                }
            }

            val userProfile = sessionManager.getUserProfile()
            val blindTestCandidates = selectBlindTestCandidates(
                allSupervisors = spvList,
                branchCode = userProfile?.branchCode,
                branchName = userProfile?.branchName,
                preselectedSpvId = selectedSpvId
            )

            _uiState.value = LeaderSurveyUiState(
                outletId = outletId,
                outletName = outlet?.namaToko ?: "Toko #$outletId",
                existingSyncId = existingSyncId,
                checkinLatitude = existingVisit?.checkinLatitude,
                checkinLongitude = existingVisit?.checkinLongitude,
                checkinAt = checkinAt,
                questions = hydratedQuestions,
                supervisors = blindTestCandidates,
                selectedSupervisorId = selectedSpvId,
                isL12Na = isL12Na,
                picAbsenceReason = picAbsenceReason,
                picAbsenceError = null,
                l9NoteError = null,
                validationError = null,
                showIncompleteModal = false,
                isSaved = false
            )

            // Ambil koordinat GPS check-in Leader secara otomatis bila izin lokasi tersedia
            val gpsResult = LocationHelper.getCurrentLocation(getApplication())
            if (gpsResult is LocationResult.Success) {
                _uiState.value = _uiState.value.copy(
                    checkinLatitude = gpsResult.latitude,
                    checkinLongitude = gpsResult.longitude
                )
            }
        }
    }

    /**
     * Memperbarui jawaban pilihan (Ya/Tidak/N/A) untuk kode pertanyaan tertentu.
     *
     * @param code Kode pertanyaan (misal: L1, L9)
     * @param answer Nilai jawaban yang dipilih
     */
    fun updateAnswer(code: String, answer: String) {
        val updated = _uiState.value.questions.map {
            if (it.code == code) it.copy(selectedAnswer = answer) else it
        }
        val shouldClearL9Error = code == "L9" && !answer.equals("Ya", ignoreCase = true)
        _uiState.value = _uiState.value.copy(
            questions = updated,
            l9NoteError = if (shouldClearL9Error) null else _uiState.value.l9NoteError,
            validationError = if (shouldClearL9Error) null else _uiState.value.validationError
        )
    }

    /**
     * Memperbarui teks catatan tambahan untuk kode pertanyaan tertentu.
     *
     * @param code Kode pertanyaan target
     * @param notes Isi catatan dari surveyor
     */
    fun updateNotes(code: String, notes: String) {
        val updated = _uiState.value.questions.map {
            if (it.code == code) it.copy(notes = notes) else it
        }
        val shouldClearL9Error = code == "L9" && notes.isNotBlank()
        _uiState.value = _uiState.value.copy(
            questions = updated,
            l9NoteError = if (shouldClearL9Error) null else _uiState.value.l9NoteError,
            validationError = if (shouldClearL9Error) null else _uiState.value.validationError
        )
    }

    /**
     * Memperbarui nilai kepuasan pelayanan (L11) pada rentang 0-10.
     *
     * @param rating Skor kepuasan dari 0 hingga 10
     */
    fun updateRating(rating: Int) {
        val updated = _uiState.value.questions.map {
            if (it.code == "L11") it.copy(rating = rating) else it
        }
        _uiState.value = _uiState.value.copy(questions = updated)
    }

    /**
     * Memilih supervisor yang dikenali oleh pemilik toko pada pertanyaan L12.
     *
     * @param spvId ID supervisor yang dipilih
     */
    fun selectSupervisor(spvId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedSupervisorId = spvId,
            isL12Na = false,
            picAbsenceError = null
        )
    }

    /**
     * Menandai pertanyaan L12 sebagai N/A karena pemilik/PIC toko tidak berada di tempat,
     * sehingga toko akan dijadwalkan untuk Revisit L12.
     */
    fun selectL12Na() {
        _uiState.value = _uiState.value.copy(
            selectedSupervisorId = null,
            isL12Na = true,
            picAbsenceError = null
        )
    }

    /**
     * Memperbarui alasan ketidakhadiran PIC untuk skenario L12 = N/A.
     *
     * @param reason Alasan spesifik PIC tidak di tempat
     */
    fun updatePicAbsenceReason(reason: String) {
        _uiState.value = _uiState.value.copy(
            picAbsenceReason = reason,
            picAbsenceError = if (reason.trim().length >= 5) null else _uiState.value.picAbsenceError
        )
    }

    /**
     * Membuka Modal Intersepsi Check-Out Dini secara manual (tombol Terkendala di header).
     */
    fun openIncompleteModal() {
        _uiState.value = _uiState.value.copy(showIncompleteModal = true)
    }

    /**
     * Menutup Modal Intersepsi Check-Out Dini agar surveyor dapat melanjutkan pengisian.
     */
    fun dismissIncompleteModal() {
        _uiState.value = _uiState.value.copy(showIncompleteModal = false)
    }

    /**
     * Menghapus pesan error validasi setelah ditampilkan ke pengguna.
     */
    fun clearValidationError() {
        _uiState.value = _uiState.value.copy(validationError = null)
    }

    /**
     * Mereset flag navigasi tersimpan agar tidak memicu popBackStack berulang.
     */
    fun consumeSavedEvent() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    /**
     * Memeriksa kelengkapan survei saat surveyor menekan Check-Out:
     * - Jika ada pertanyaan yang belum terjawab, memunculkan Modal Intersepsi Check-Out Dini (Bab 5).
     * - Jika lengkap, menegakkan validasi L9 dan L12 = N/A sebelum menyimpan ke Room lokal.
     */
    fun requestCheckout() {
        val state = _uiState.value

        // 1. Cek apakah seluruh butir pertanyaan (L1-L12 serta pertanyaan dinamis aktif) telah diisi
        val hasIncomplete = state.questions.any { q ->
            when {
                q.isRatingType -> q.rating == null
                q.isPhotoChoiceType -> state.selectedSupervisorId == null && !state.isL12Na
                q.isTextNotesType -> q.selectedAnswer.isNullOrBlank() && q.notes.isBlank()
                else -> q.selectedAnswer.isNullOrBlank()
            }
        }
        if (hasIncomplete) {
            _uiState.value = state.copy(showIncompleteModal = true)
            return
        }

        // 2. Validasi bisnis L9: Jika terdapat reward yang belum dicairkan ("Ya"), catatan wajib diisi
        val l9Item = state.questions.find { it.code == "L9" }
        if (l9Item != null &&
            l9Item.selectedAnswer.equals("Ya", ignoreCase = true) &&
            l9Item.notes.isBlank()
        ) {
            _uiState.value = state.copy(
                l9NoteError = "Wajib isi detail reward yang belum diterima.",
                validationError = "Catatan pada pertanyaan L9 wajib diisi jika jawabannya Ya."
            )
            return
        }

        // 3. Validasi skenario L12 = N/A (Bab 2C.3): Wajib isi pic_absence_reason min 5 karakter
        if (state.isL12Na && state.picAbsenceReason.trim().length < 5) {
            _uiState.value = state.copy(
                picAbsenceError = "Alasan PIC tidak di tempat wajib diisi (min. 5 karakter).",
                validationError = "Isi alasan ketidakhadiran PIC pada L12 terlebih dahulu."
            )
            return
        }

        // 4. Simpan kuesioner lengkap ke Room lokal
        viewModelScope.launch {
            val today = todayDateString()
            val now = nowTimestampString()

            val responses = state.questions.map { q ->
                when {
                    q.isRatingType -> LeaderQuestionResponseDto(
                        questionCode = q.code,
                        answerValue = (q.rating ?: 0).toString(),
                        notes = q.notes.trim().ifBlank { null },
                        ratingValue = q.rating
                    )
                    q.isPhotoChoiceType -> LeaderQuestionResponseDto(
                        questionCode = q.code,
                        answerValue = if (state.isL12Na) {
                            "N/A"
                        } else {
                            resolveL12AnswerValue(state.supervisors, state.selectedSupervisorId)
                        },
                        notes = if (state.isL12Na) state.picAbsenceReason.trim() else null,
                        selectedSupervisorId = if (state.isL12Na) null else state.selectedSupervisorId
                    )
                    q.isTextNotesType -> LeaderQuestionResponseDto(
                        questionCode = q.code,
                        answerValue = q.selectedAnswer?.takeIf { it.isNotBlank() }
                            ?: q.notes.trim().ifBlank { "N/A" },
                        notes = q.notes.trim().ifBlank { null }
                    )
                    else -> LeaderQuestionResponseDto(
                        questionCode = q.code,
                        answerValue = q.selectedAnswer ?: "N/A",
                        notes = q.notes.trim().ifBlank { null }
                    )
                }
            }

            val trimmedPicReason = if (state.isL12Na) state.picAbsenceReason.trim() else null
            val request = LeaderSurveyRequest(
                offlineSyncId = state.existingSyncId ?: UUID.randomUUID().toString(),
                outletId = state.outletId,
                visitDate = today,
                checkinLatitude = state.checkinLatitude,
                checkinLongitude = state.checkinLongitude,
                checkinAt = state.checkinAt ?: now,
                checkoutAt = now,
                notes = if (state.isL12Na) {
                    "Survey selesai dengan L12 N/A (Revisit L12)"
                } else {
                    "Survey selesai diisi via mobile"
                },
                picAbsenceReason = trimmedPicReason,
                isIncomplete = false,
                needsRevisit = state.isL12Na,
                revisitNotes = if (state.isL12Na) {
                    "Lakukan penilaian susulan L12 pada jadwal berikutnya"
                } else {
                    null
                },
                responses = responses
            )

            val l12Score = when {
                state.isL12Na -> null
                resolveL12AnswerValue(state.supervisors, state.selectedSupervisorId) == "Ya" -> 100.0
                else -> 0.0
            }
            surveyRepository.saveLeaderSurveyLocally(request, state.outletName, localScore = l12Score)
            SyncManager.enqueueImmediateSync(getApplication())

            _uiState.value = _uiState.value.copy(
                l9NoteError = null,
                picAbsenceError = null,
                validationError = null,
                isSaved = true
            )
        }
    }

    /**
     * Menyimpan kunjungan dengan status Check-Out Dini (`is_incomplete = true`, `needs_revisit = true`)
     * dari Modal Intersepsi Bab 5 ke penyimpanan lokal Room.
     *
     * @param reasonType Kode alasan standar (misal: `busy_store`, `pic_absent`, `other`)
     * @param reasonText Penjelasan alasan kendala di lapangan
     */
    fun submitIncompleteCheckout(reasonType: String, reasonText: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val today = todayDateString()
            val now = nowTimestampString()

            // Pertahankan jawaban parsial yang sudah sempat diisi (jika ada)
            val partialResponses = state.questions.mapNotNull { q ->
                when {
                    q.isRatingType -> q.rating?.let { r ->
                        LeaderQuestionResponseDto(
                            questionCode = q.code,
                            answerValue = r.toString(),
                            ratingValue = r
                        )
                    }
                    q.isPhotoChoiceType -> when {
                        state.isL12Na -> LeaderQuestionResponseDto(
                            questionCode = q.code,
                            answerValue = "N/A",
                            notes = state.picAbsenceReason.trim().ifBlank { reasonText }
                        )
                        state.selectedSupervisorId != null -> LeaderQuestionResponseDto(
                            questionCode = q.code,
                            answerValue = resolveL12AnswerValue(state.supervisors, state.selectedSupervisorId),
                            selectedSupervisorId = state.selectedSupervisorId
                        )
                        else -> null
                    }
                    q.isTextNotesType -> {
                        val trimmed = q.notes.trim()
                        val ans = q.selectedAnswer?.takeIf { it.isNotBlank() } ?: trimmed
                        if (ans.isNotBlank()) {
                            LeaderQuestionResponseDto(
                                questionCode = q.code,
                                answerValue = ans,
                                notes = trimmed.ifBlank { null }
                            )
                        } else {
                            null
                        }
                    }
                    else -> q.selectedAnswer?.let { ans ->
                        LeaderQuestionResponseDto(
                            questionCode = q.code,
                            answerValue = ans,
                            notes = q.notes.trim().ifBlank { null }
                        )
                    }
                }
            }

            val request = LeaderSurveyRequest(
                offlineSyncId = state.existingSyncId ?: UUID.randomUUID().toString(),
                outletId = state.outletId,
                visitDate = today,
                checkinLatitude = state.checkinLatitude,
                checkinLongitude = state.checkinLongitude,
                checkinAt = state.checkinAt ?: now,
                checkoutAt = now,
                notes = reasonText,
                picAbsenceReason = if (reasonType == "pic_absent") reasonText else null,
                isIncomplete = true,
                incompleteReasonType = reasonType,
                incompleteReason = reasonText,
                needsRevisit = true,
                revisitNotes = "Jadwalkan kunjungan susulan untuk melengkapi survei Leader",
                responses = partialResponses
            )

            surveyRepository.saveLeaderSurveyLocally(request, state.outletName, localScore = 0.0)
            SyncManager.enqueueImmediateSync(getApplication())

            _uiState.value = _uiState.value.copy(
                showIncompleteModal = false,
                isSaved = true
            )
        }
    }

    private fun defaultLeaderQuestions(): List<QuestionItem> = listOf(
        QuestionItem("L1", "Kunjungan Sales", "Apakah Sales mengunjungi toko ini dalam 14 hari terakhir?", allowNa = false),
        QuestionItem("L2", "Kunjungan Sales", "Apakah Sales melakukan penataan display ketika berkunjung ke toko?"),
        QuestionItem("L3", "Kunjungan Sales", "Apakah Sales menanyakan kebutuhan stok atau pesanan toko saat kunjungan?"),
        QuestionItem("L4", "Informasi Program", "Apakah Sales menjelaskan program marketing yang sedang berjalan saat kunjungan? (Prioritas produk SHP & ATB, potongan harga, bonus barang)"),
        QuestionItem("L5", "Kejelasan Program", "Apakah Sales menyampaikan syarat dan periode program tersebut?"),
        QuestionItem("L6", "Pemenuhan Pesanan", "Apakah pesanan barang tiba pada atau sebelum tanggal yang dijanjikan kepada toko?"),
        QuestionItem("L7", "Kesesuaian Pesanan", "Apakah barang pesanan yang tiba dalam kondisi baik (tidak rusak dan tidak kedaluwarsa)?"),
        QuestionItem("L8", "Penanganan Keluhan", "Dalam 3 bulan terakhir, apakah keluhan atau retur terakhir yang diajukan toko sudah diselesaikan?"),
        QuestionItem("L9", "Penerimaan Reward", "Apakah ada pencairan reward yang belum diterima? (Wajib isi catatan jika jawaban Ya)"),
        QuestionItem("L10", "Tindak Lanjut Masukan", "Apakah masukan atau saran terakhir yang toko sampaikan kepada Sales sudah mendapat tanggapan?", answerType = "text_notes"),
        QuestionItem("L11", "Kepuasan Pelayanan", "Tingkat kepuasan pemilik toko terhadap pelayanan tim Sales (Skor 0-10)", answerType = "rating_0_10"),
        QuestionItem("L12", "Pengenalan SPV", "Toko diminta mengenali FOTO SPV dari 3 pilihan foto yang diperlihatkan", answerType = "photo_choice")
    )

    /**
     * Memilih 3 kandidat foto SPV untuk blind test L12: 1 SPV target (yang sesuai cabang/distributor)
     * ditambah 2 SPV pengecoh acak, lalu mengacak urutan kartu posisi 1..3.
     */
    private fun selectBlindTestCandidates(
        allSupervisors: List<SupervisorEntity>,
        branchCode: String?,
        branchName: String?,
        preselectedSpvId: Long? = null
    ): List<SupervisorEntity> {
        if (allSupervisors.isEmpty()) return emptyList()
        if (allSupervisors.size <= 3) {
            val hasExplicitTarget = allSupervisors.any { it.isTarget }
            return if (hasExplicitTarget) {
                allSupervisors.shuffled()
            } else {
                val target = findMatchingSupervisor(allSupervisors, branchCode, branchName) ?: allSupervisors.first()
                allSupervisors.map { it.copy(isTarget = it.id == target.id) }.shuffled()
            }
        }

        val rawTarget = allSupervisors.firstOrNull { it.isTarget }
            ?: findMatchingSupervisor(allSupervisors, branchCode, branchName)
            ?: allSupervisors.first()
        val targetSpv = rawTarget.copy(isTarget = true)

        val distractorsPool = allSupervisors.filter { it.id != targetSpv.id }.shuffled()
        val selectedDistractors = buildList {
            preselectedSpvId?.let { preId ->
                distractorsPool.firstOrNull { it.id == preId }?.let { add(it.copy(isTarget = false)) }
            }
            for (candidate in distractorsPool) {
                if (size >= 2) break
                if (none { it.id == candidate.id }) {
                    add(candidate.copy(isTarget = false))
                }
            }
        }

        return (listOf(targetSpv) + selectedDistractors).shuffled()
    }

    private fun findMatchingSupervisor(
        supervisors: List<SupervisorEntity>,
        branchCode: String?,
        branchName: String?
    ): SupervisorEntity? {
        val genericWords = setOf("distributor", "asw", "foods", "pt", "cv", "cabang", "area", "br", "01", "02", "03")
        val tokens = buildSet {
            branchCode?.trim()?.takeIf { it.isNotBlank() }?.let { code ->
                add(code.lowercase())
                code.split("-", "_", " ").map { it.trim().lowercase() }
                    .filter { it.length >= 3 && it !in genericWords }
                    .forEach { add(it) }
            }
            branchName?.trim()?.takeIf { it.isNotBlank() }?.let { name ->
                name.split(" ", "-", "/").map { it.trim().lowercase() }
                    .filter { it.length >= 3 && it !in genericWords }
                    .forEach { add(it) }
            }
        }
        if (tokens.isEmpty()) return null
        return supervisors.firstOrNull { spv ->
            val haystack = listOfNotNull(
                spv.coverageDistributors,
                spv.areaName,
                spv.salesCode,
                spv.nip
            ).joinToString(" ").lowercase()
            tokens.any { token -> haystack.contains(token) }
        }
    }

    private fun resolveL12AnswerValue(
        supervisors: List<SupervisorEntity>,
        selectedId: Long?
    ): String {
        val selectedSpv = supervisors.find { it.id == selectedId }
        val hasExplicitTarget = supervisors.any { it.isTarget }
        return if (!hasExplicitTarget || selectedSpv?.isTarget == true) "Ya" else "Tidak"
    }

    private fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun nowTimestampString(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}
