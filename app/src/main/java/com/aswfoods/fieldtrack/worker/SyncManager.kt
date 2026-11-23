package com.aswfoods.fieldtrack.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Pengelola pemicu sinkronisasi latar belakang WorkManager.
 * Memastikan pengiriman data antrean hanya dieksekusi saat ada koneksi internet.
 */
object SyncManager {

    private const val ONE_TIME_SYNC_TAG = "fieldtrack_immediate_sync"
    private const val PERIODIC_SYNC_TAG = "fieldtrack_periodic_sync"

    /**
     * Memicu sinkronisasi instan saat surveyor menekan tombol 'Push Data Pending'
     * atau setelah selesai mengisi formulir survei jika jaringan tersedia.
     *
     * @param context Application context
     */
    fun enqueueImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(ONE_TIME_SYNC_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_TAG,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Menjadwalkan sinkronisasi berkala setiap 15 menit jika ada jaringan aktif.
     *
     * @param context Application context
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(PERIODIC_SYNC_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}

