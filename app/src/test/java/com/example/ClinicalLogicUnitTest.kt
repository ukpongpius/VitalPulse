package com.example

import com.example.data.model.NutritionItem
import com.example.ui.DailyMacroSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalLogicUnitTest {

    @Test
    fun testBloodPressureAhaClassification() {
        fun classifyBp(sys: Double, dia: Double): String {
            return when {
                sys > 180 || dia > 120 -> "Crisis"
                sys >= 140 || dia >= 90 -> "Stage 2"
                sys in 130.0..139.9 || dia in 80.0..89.9 -> "Stage 1"
                sys in 120.0..129.9 && dia < 80.0 -> "Elevated"
                else -> "Normal"
            }
        }

        assertEquals("Normal", classifyBp(118.0, 76.0))
        assertEquals("Elevated", classifyBp(124.0, 78.0))
        assertEquals("Stage 1", classifyBp(132.0, 82.0))
        assertEquals("Stage 2", classifyBp(144.0, 92.0))
        assertEquals("Crisis", classifyBp(185.0, 110.0))
        assertEquals("Crisis", classifyBp(150.0, 125.0))
    }

    @Test
    fun testMeanArterialPressureAndPulsePressure() {
        val systolic = 120.0
        val diastolic = 80.0

        val map = (diastolic + ((systolic - diastolic) / 3.0)).toInt()
        val pulsePressure = (systolic - diastolic).toInt()

        assertEquals(93, map)
        assertEquals(40, pulsePressure)
    }

    @Test
    fun testSpO2Classification() {
        fun classifySpO2(spo2: Double): String {
            return if (spo2 >= 95.0) "Normal (Optimal)" else if (spo2 >= 90.0) "Mild Hypoxemia" else "Urgent Warning"
        }

        assertEquals("Normal (Optimal)", classifySpO2(98.0))
        assertEquals("Normal (Optimal)", classifySpO2(95.0))
        assertEquals("Mild Hypoxemia", classifySpO2(92.0))
        assertEquals("Urgent Warning", classifySpO2(88.0))
    }

    @Test
    fun testTemperatureClassification() {
        fun classifyTemp(tempF: Double): String {
            return when {
                tempF >= 100.4 -> "High Fever"
                tempF >= 99.5 -> "Low Grade Fever"
                tempF < 97.0 -> "Hypothermia"
                else -> "Normal"
            }
        }

        assertEquals("Normal", classifyTemp(98.6))
        assertEquals("Low Grade Fever", classifyTemp(99.8))
        assertEquals("High Fever", classifyTemp(101.5))
        assertEquals("Hypothermia", classifyTemp(96.5))
    }

    @Test
    fun testBmiAndTdeeFormulas() {
        val weightKg = 72.0
        val heightCm = 178.0
        val age = 29
        val heightM = heightCm / 100.0

        val bmi = weightKg / (heightM * heightM)
        assertTrue(bmi in 22.5..23.0)

        // Mifflin-St Jeor formula for Men: 10*weight + 6.25*height - 5*age + 5
        val bmr = (10 * weightKg + 6.25 * heightCm - 5 * age + 5).toInt()
        assertEquals(1692, bmr)

        // Moderate activity multiplier = 1.55
        val tdee = (bmr * 1.55).toInt()
        assertEquals(2622, tdee)
    }

    @Test
    fun testMacroAggregation() {
        val meals = listOf(
            NutritionItem(
                date = "2026-10-30",
                mealType = "Breakfast",
                foodName = "Oatmeal",
                calories = 300,
                proteinGrams = 10.0,
                carbsGrams = 50.0,
                fatGrams = 5.0,
                fiberGrams = 8.0
            ),
            NutritionItem(
                date = "2026-10-30",
                mealType = "Lunch",
                foodName = "Chicken Salad",
                calories = 450,
                proteinGrams = 40.0,
                carbsGrams = 20.0,
                fatGrams = 12.0,
                fiberGrams = 6.0
            )
        )

        val totalCalories = meals.sumOf { it.calories }
        val totalProtein = meals.sumOf { it.proteinGrams }
        val totalCarbs = meals.sumOf { it.carbsGrams }
        val totalFat = meals.sumOf { it.fatGrams }
        val totalFiber = meals.sumOf { it.fiberGrams }

        assertEquals(750, totalCalories)
        assertEquals(50.0, totalProtein, 0.01)
        assertEquals(70.0, totalCarbs, 0.01)
        assertEquals(17.0, totalFat, 0.01)
        assertEquals(14.0, totalFiber, 0.01)
    }
}
