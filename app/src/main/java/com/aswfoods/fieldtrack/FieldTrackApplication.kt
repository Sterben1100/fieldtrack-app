package com.aswfoods.fieldtrack

import android.app.Application
import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.data.local.database.AppDatabase
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.worker.SyncManager

/**
 * Kelas Application utama FieldTrack Pro.
 * Menginisialisasi penyedia token terenkripsi, basis data lokal, dan menjadwalkan sync periodik di latar belakang.
 */
class FieldTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Hubungkan penyedia token terenkripsi dan status Mode Demo ke RetrofitClient
        val sessionManager = SessionManager(this)
        RetrofitClient.configure(
            url = sessionManager.getBaseUrl(),
            demoProvider = { sessionManager.isDemoMode() },
            provider = { sessionManager.getToken() }
        )

        // Inisialisasi basis data Room lokal
        AppDatabase.getDatabase(this)

        // Jadwalkan sinkronisasi berkala (setiap 15 menit jika terhubung internet)
        SyncManager.schedulePeriodicSync(this)
    }
}
