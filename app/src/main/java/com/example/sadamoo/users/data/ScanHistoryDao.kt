package com.example.sadamoo.users.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface ScanHistoryDao {

    // ✅ INSERT OPERATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scanHistory: ScanHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scanHistories: List<ScanHistory>): List<Long>

    // ✅ UPDATE OPERATIONS
    @Update
    suspend fun update(scanHistory: ScanHistory): Int

    @Query("UPDATE scan_history SET result = :result WHERE id = :id")
    suspend fun updateResult(id: Int, result: String): Int

    @Query("UPDATE scan_history SET diseaseInfo = :diseaseInfo WHERE id = :id")
    suspend fun updateDiseaseInfo(id: Int, diseaseInfo: String?): Int

    // ✅ DELETE OPERATIONS
    @Delete
    suspend fun delete(scanHistory: ScanHistory): Int

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM scan_history WHERE timestamp < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: Date): Int

    // ✅ SELECT OPERATIONS - Basic Queries
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScanHistory(): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    suspend fun getAllScanHistorySync(): List<ScanHistory>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanHistoryById(id: Int): ScanHistory?

    @Query("SELECT * FROM scan_history WHERE id = :id")
    fun getScanHistoryByIdLive(id: Int): LiveData<ScanHistory?>

    // ✅ SELECT OPERATIONS - Filtered Queries
    @Query("SELECT * FROM scan_history WHERE result LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun searchScanHistory(searchQuery: String): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE diseaseInfo IS NOT NULL ORDER BY timestamp DESC")
    fun getDiseaseDetections(): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE diseaseInfo IS NULL ORDER BY timestamp DESC")
    fun getHealthyScans(): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE diseaseInfo = :diseaseName ORDER BY timestamp DESC")
    fun getScansByDisease(diseaseName: String): LiveData<List<ScanHistory>>

    // ✅ SELECT OPERATIONS - Date Range Queries
    @Query("SELECT * FROM scan_history WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getScanHistoryByDateRange(startDate: Date, endDate: Date): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now') ORDER BY timestamp DESC")
    fun getTodayScans(): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE DATE(timestamp/1000, 'unixepoch') >= DATE('now', '-7 days') ORDER BY timestamp DESC")
    fun getLastWeekScans(): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE DATE(timestamp/1000, 'unixepoch') >= DATE('now', '-30 days') ORDER BY timestamp DESC")
    fun getLastMonthScans(): LiveData<List<ScanHistory>>

    // ✅ SELECT OPERATIONS - Pagination
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getScanHistoryPaged(limit: Int, offset: Int): List<ScanHistory>

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScans(limit: Int): LiveData<List<ScanHistory>>

    // ✅ COUNT OPERATIONS
    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getTotalScanCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history")
    fun getTotalScanCountLive(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM scan_history WHERE diseaseInfo IS NOT NULL")
    suspend fun getDiseaseDetectionCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE diseaseInfo IS NULL")
    suspend fun getHealthyScanCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE diseaseInfo = :diseaseName")
    suspend fun getCountByDisease(diseaseName: String): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayScanCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE DATE(timestamp/1000, 'unixepoch') >= DATE('now', '-7 days')")
    suspend fun getWeeklyScanCount(): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE DATE(timestamp/1000, 'unixepoch') >= DATE('now', '-30 days')")
    suspend fun getMonthlyScanCount(): Int

    // ✅ STATISTICS OPERATIONS
    @Query("SELECT AVG(confidence) FROM scan_history")
    suspend fun getAverageConfidence(): Float?

    @Query("SELECT AVG(confidence) FROM scan_history WHERE diseaseInfo IS NOT NULL")
    suspend fun getAverageConfidenceForDiseases(): Float?

    @Query("SELECT AVG(confidence) FROM scan_history WHERE diseaseInfo IS NULL")
    suspend fun getAverageConfidenceForHealthy(): Float?

    @Query("SELECT diseaseInfo, COUNT(*) as count FROM scan_history WHERE diseaseInfo IS NOT NULL GROUP BY diseaseInfo ORDER BY count DESC")
    suspend fun getDiseaseStatistics(): List<DiseaseStatistic>

    @Query("SELECT DATE(timestamp/1000, 'unixepoch') as date, COUNT(*) as count FROM scan_history GROUP BY DATE(timestamp/1000, 'unixepoch') ORDER BY date DESC LIMIT 30")
    suspend fun getDailyScanCounts(): List<DailyScanCount>

    // ✅ ADVANCED QUERIES
    @Query("SELECT * FROM scan_history WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    fun getHighConfidenceScans(minConfidence: Float): LiveData<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE confidence < :maxConfidence ORDER BY timestamp DESC")
    fun getLowConfidenceScans(maxConfidence: Float): LiveData<List<ScanHistory>>

    @Query("SELECT DISTINCT diseaseInfo FROM scan_history WHERE diseaseInfo IS NOT NULL ORDER BY diseaseInfo")
    suspend fun getUniqueDetectedDiseases(): List<String>

    @Query("SELECT * FROM scan_history WHERE imagePath = :imagePath LIMIT 1")
    suspend fun getScanByImagePath(imagePath: String): ScanHistory?

    // ✅ CLEANUP OPERATIONS
    @Query("DELETE FROM scan_history WHERE id NOT IN (SELECT id FROM scan_history ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun keepOnlyRecentScans(keepCount: Int): Int

    @Query("DELETE FROM scan_history WHERE confidence < :minConfidence")
    suspend fun deleteLowConfidenceScans(minConfidence: Float): Int

    // ✅ BATCH OPERATIONS
    @Query("UPDATE scan_history SET diseaseInfo = :newDiseaseInfo WHERE diseaseInfo = :oldDiseaseInfo")
    suspend fun updateDiseaseInfoBatch(oldDiseaseInfo: String, newDiseaseInfo: String): Int

    @Query("SELECT * FROM scan_history WHERE id IN (:ids)")
    suspend fun getScanHistoriesByIds(ids: List<Int>): List<ScanHistory>

    @Query("DELETE FROM scan_history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>): Int
}

// ✅ DATA CLASSES FOR STATISTICS
data class DiseaseStatistic(
    val diseaseInfo: String,
    val count: Int
)

data class DailyScanCount(
    val date: String,
    val count: Int
)
