package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "asset_dossier")
data class AssetDossier(
  @PrimaryKey val id: Int = 1,
  val name: String,
  val series: String,
  val status: String, // e.g. "MAINTENANCE REQUIRED", "SECURED", "MISSING"
  val custodianName: String,
  val custodianDivision: String,
  val serialReference: String,
  val classification: String,
  val acquisitionDate: String,
  val lastLocation: String,
  val uid: String
)

@Entity(tableName = "dossier_logs")
data class DossierLog(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val logNumber: String,
  val category: String, // e.g. "MAINTENANCE", "GENERAL", "CRITICAL"
  val dateStr: String,
  val notes: String,
  val author: String,
  val isCritical: Boolean = false
)

// ==========================================
// 2. Data Access Object (DAO)
// ==========================================

@Dao
interface DossierDao {
  @Query("SELECT * FROM asset_dossier WHERE id = 1 LIMIT 1")
  fun getAssetDossier(): Flow<AssetDossier?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAssetDossier(dossier: AssetDossier)

  @Query("SELECT * FROM dossier_logs ORDER BY id DESC")
  fun getAllLogs(): Flow<List<DossierLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: DossierLog)

  @Query("SELECT COUNT(*) FROM asset_dossier")
  suspend fun getAssetCount(): Int

  @Query("SELECT COUNT(*) FROM dossier_logs")
  suspend fun getLogCount(): Int
}

// ==========================================
// 3. Database
// ==========================================

@Database(entities = [AssetDossier::class, DossierLog::class], version = 1, exportSchema = false)
abstract class DossierDatabase : RoomDatabase() {
  abstract fun dossierDao(): DossierDao

  companion object {
    @Volatile
    private var INSTANCE: DossierDatabase? = null

    fun getDatabase(context: Context): DossierDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          DossierDatabase::class.java,
          "asset_track_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}

// ==========================================
// 4. Repository (Abstracted Data Layer)
// ==========================================

class DossierRepository(private val dossierDao: DossierDao) {
  val assetDossier: Flow<AssetDossier?> = dossierDao.getAssetDossier()
  val allLogs: Flow<List<DossierLog>> = dossierDao.getAllLogs()

  suspend fun updateAsset(dossier: AssetDossier) {
    dossierDao.insertAssetDossier(dossier)
  }

  suspend fun addLog(log: DossierLog) {
    dossierDao.insertLog(log)
  }

  suspend fun checkAndPrepopulate() {
    if (dossierDao.getAssetCount() == 0) {
      dossierDao.insertAssetDossier(
        AssetDossier(
          id = 1,
          name = "Laser Alignment Rig X-200",
          series = "Industrial Precision Series | OptiCore Systems",
          status = "MAINTENANCE REQUIRED",
          custodianName = "Jameson Dax",
          custodianDivision = "Logistics Division",
          serialReference = "SN-44029-BRV",
          classification = "Precision Optics",
          acquisitionDate = "14 OCT 2023",
          lastLocation = "Bay 4, Sector G",
          uid = "UID-992-XC-2024"
        )
      )
    }

    if (dossierDao.getLogCount() == 0) {
      dossierDao.insertLog(
        DossierLog(
          logNumber = "LOG #8829",
          category = "MAINTENANCE",
          dateStr = "22 JAN 2024",
          notes = "Scheduled sensor calibration performed. Accuracy variance corrected from 0.05mm to 0.001mm. Cooling fan inspected and cleaned. Equipment ready for high-load operations. Observations indicate minor wear on housing.",
          author = "ADMIN_R02",
          isCritical = true
        )
      )
      dossierDao.insertLog(
        DossierLog(
          logNumber = "LOG #8712",
          category = "GENERAL",
          dateStr = "15 DEC 2023",
          notes = "Primary lens replaced with UV-coated alternative for specific client project requirements. Original lens stored in Logistics Cabinet 09. Subject under surveillance.",
          author = "LOGISTICS_D09",
          isCritical = false
        )
      )
    }
  }
}
