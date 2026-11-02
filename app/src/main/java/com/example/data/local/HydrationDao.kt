package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HydrationLog
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getHydrationForDate(date: String): Flow<List<HydrationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydration(log: HydrationLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<HydrationLog>)

    @Query("DELETE FROM hydration_logs WHERE id = :id")
    suspend fun deleteHydrationById(id: Long)

    @Query("SELECT COUNT(*) FROM hydration_logs")
    suspend fun countLogs(): Int
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
