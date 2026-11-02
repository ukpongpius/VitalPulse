package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_logs")
data class HydrationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val amountMl: Int
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Alex",
    val age: Int = 29,
    val gender: String = "Male",
    val weightKg: Double = 72.0,
    val heightCm: Double = 178.0,
    val activityLevel: String = "Moderate", // Sedentary, Light, Moderate, Very Active
    val dailyCalorieGoal: Int = 2200,
    val dailyProteinGoal: Int = 140,
    val dailyCarbsGoal: Int = 225,
    val dailyFatGoal: Int = 65,
    val dailyWaterGoalMl: Int = 2500
)
