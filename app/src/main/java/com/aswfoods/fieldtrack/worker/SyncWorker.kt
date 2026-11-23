package com.aswfoods.fieldtrack.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aswfoods.fieldtrack.data.local.database.AppDatabase
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.data.repository.SurveyRepository

/**
 * Worker latar belakang untuk mengeksekusi sinkronisasi antrean survey offline secara otomatis
 * ketika perangkat terdeteksi terhubung kembali ke jaringan internet.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Menjalankan pekerjaan background sync.
     *
     * @return Result status eksekusi WorkManager
     */
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val apiService = RetrofitClient.apiService
            val repository = SurveyRepository(apiService, database.pendingVisitDao())

            val syncResult = repository.pushPendingVisits()

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                // Minta penjadwalan ulang jika gagal karena jaringan
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

