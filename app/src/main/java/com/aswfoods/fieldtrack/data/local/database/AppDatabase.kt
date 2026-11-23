package com.aswfoods.fieldtrack.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aswfoods.fieldtrack.data.local.dao.OutletDao
import com.aswfoods.fieldtrack.data.local.dao.PendingVisitDao
import com.aswfoods.fieldtrack.data.local.dao.ProductDao
import com.aswfoods.fieldtrack.data.local.dao.SupervisorDao
import com.aswfoods.fieldtrack.data.local.dao.SurveyQuestionDao
import com.aswfoods.fieldtrack.data.local.entity.OutletEntity
import com.aswfoods.fieldtrack.data.local.entity.PendingVisitEntity
import com.aswfoods.fieldtrack.data.local.entity.ProductEntity
import com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity
import com.aswfoods.fieldtrack.data.local.entity.SurveyQuestionEntity

/**
 * Basis data SQLite lokal Room untuk mendukung operasional penuh aplikasi secara offline.
 */
@Database(
    entities = [
        OutletEntity::class,
        ProductEntity::class,
        SupervisorEntity::class,
        PendingVisitEntity::class,
        SurveyQuestionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun outletDao(): OutletDao
    abstract fun productDao(): ProductDao
    abstract fun supervisorDao(): SupervisorDao
    abstract fun pendingVisitDao(): PendingVisitDao
    abstract fun surveyQuestionDao(): SurveyQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton pattern untuk mendapatkan instans database lokal.
         *
         * @param context Application context
         * @return Instans AppDatabase
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fieldtrack_asw_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
