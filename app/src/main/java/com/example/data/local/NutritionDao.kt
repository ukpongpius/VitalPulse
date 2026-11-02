package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NutritionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_items WHERE date = :date ORDER BY timestamp ASC")
    fun getMealsForDate(date: String): Flow<List<NutritionItem>>

    @Query("SELECT * FROM nutrition_items ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<NutritionItem>>

    @Query("SELECT * FROM nutrition_items ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMeals(limit: Int): Flow<List<NutritionItem>>

    @Query("SELECT * FROM nutrition_items WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getMealsSince(sinceTimestamp: Long): Flow<List<NutritionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: NutritionItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<NutritionItem>)

    @Query("DELETE FROM nutrition_items WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("SELECT COUNT(*) FROM nutrition_items")
    suspend fun countMeals(): Int
}
