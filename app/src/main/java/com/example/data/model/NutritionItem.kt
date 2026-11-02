package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_items")
data class NutritionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val foodName: String,
    val portionSize: String = "1 serving",
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double = 0.0,
    val sugarGrams: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val healthRating: String = "Healthy" // Superfood, Healthy, Balanced, Treat
)

data class PresetFood(
    val name: String,
    val defaultPortion: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val category: String, // Breakfast, Lunch, Dinner, Snack
    val tag: String // Superfood, Lean Protein, Whole Grain, etc.
)
