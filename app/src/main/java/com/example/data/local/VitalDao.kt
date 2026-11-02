package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VitalSign
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalDao {
    @Query("SELECT * FROM vital_signs ORDER BY timestamp DESC")
    fun getAllVitals(): Flow<List<VitalSign>>

    @Query("SELECT * FROM vital_signs WHERE type = :type ORDER BY timestamp DESC")
    fun getVitalsByType(type: String): Flow<List<VitalSign>>

    @Query("SELECT * FROM vital_signs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentVitals(limit: Int): Flow<List<VitalSign>>

    @Query("SELECT * FROM vital_signs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getVitalsSince(sinceTimestamp: Long): Flow<List<VitalSign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVital(vital: VitalSign): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vitals: List<VitalSign>)

    @Query("DELETE FROM vital_signs WHERE id = :id")
    suspend fun deleteVitalById(id: Long)

    @Query("SELECT COUNT(*) FROM vital_signs")
    suspend fun countVitals(): Int
}
