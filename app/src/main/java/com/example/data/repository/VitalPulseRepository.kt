package com.example.data.repository

import com.example.data.local.HydrationDao
import com.example.data.local.NutritionDao
import com.example.data.local.ProfileDao
import com.example.data.local.VitalDao
import com.example.data.model.HydrationLog
import com.example.data.model.NutritionItem
import com.example.data.model.PresetFood
import com.example.data.model.UserProfile
import com.example.data.model.VitalSign
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VitalPulseRepository(
    private val vitalDao: VitalDao,
    private val nutritionDao: NutritionDao,
    private val hydrationDao: HydrationDao,
    private val profileDao: ProfileDao
) {
    val allVitals: Flow<List<VitalSign>> = vitalDao.getAllVitals()
    val allMeals: Flow<List<NutritionItem>> = nutritionDao.getAllMeals()
    val userProfile: Flow<UserProfile?> = profileDao.getProfile()

    fun getVitalsByType(type: String): Flow<List<VitalSign>> = vitalDao.getVitalsByType(type)
    fun getRecentVitals(limit: Int = 20): Flow<List<VitalSign>> = vitalDao.getRecentVitals(limit)
    fun getVitalsSince(sinceTimestamp: Long): Flow<List<VitalSign>> = vitalDao.getVitalsSince(sinceTimestamp)

    fun getMealsForDate(date: String): Flow<List<NutritionItem>> = nutritionDao.getMealsForDate(date)
    fun getHydrationForDate(date: String): Flow<List<HydrationLog>> = hydrationDao.getHydrationForDate(date)

    suspend fun logVital(vital: VitalSign): Long = vitalDao.insertVital(vital)
    suspend fun deleteVital(id: Long) = vitalDao.deleteVitalById(id)

    suspend fun logMeal(meal: NutritionItem): Long = nutritionDao.insertMeal(meal)
    suspend fun deleteMeal(id: Long) = nutritionDao.deleteMealById(id)

    suspend fun logHydration(amountMl: Int, date: String): Long {
        return hydrationDao.insertHydration(
            HydrationLog(
                date = date,
                amountMl = amountMl
            )
        )
    }

    suspend fun updateProfile(profile: UserProfile) = profileDao.insertOrUpdateProfile(profile)

    suspend fun initializeSeedDataIfNeeded() {
        val vitalsCount = vitalDao.countVitals()
        val mealsCount = nutritionDao.countMeals()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if (profileDao.getProfile() == null) {
            profileDao.insertOrUpdateProfile(UserProfile())
        }

        if (vitalsCount == 0) {
            val sampleVitals = mutableListOf<VitalSign>()
            // Generate realistic 7-day data
            for (dayOffset in 6 downTo 0) {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -dayOffset)
                }

                // Morning Resting Heart Rate
                cal.set(Calendar.HOUR_OF_DAY, 7)
                cal.set(Calendar.MINUTE, 30)
                sampleVitals.add(
                    VitalSign(
                        timestamp = cal.timeInMillis,
                        type = "HEART_RATE",
                        primaryValue = (64..72).random().toDouble(),
                        unit = "BPM",
                        status = "Normal",
                        contextNote = "Morning resting pulse"
                    )
                )

                // Blood Pressure
                cal.set(Calendar.HOUR_OF_DAY, 8)
                val systolic = (116..122).random().toDouble()
                val diastolic = (74..80).random().toDouble()
                sampleVitals.add(
                    VitalSign(
                        timestamp = cal.timeInMillis,
                        type = "BLOOD_PRESSURE",
                        primaryValue = systolic,
                        secondaryValue = diastolic,
                        unit = "mmHg",
                        status = if (systolic < 120 && diastolic < 80) "Normal" else "Elevated",
                        contextNote = "Seated resting reading"
                    )
                )

                // SpO2
                cal.set(Calendar.HOUR_OF_DAY, 12)
                sampleVitals.add(
                    VitalSign(
                        timestamp = cal.timeInMillis,
                        type = "SPO2",
                        primaryValue = (98..99).random().toDouble(),
                        unit = "%",
                        status = "Normal",
                        contextNote = "Midday baseline"
                    )
                )

                // Respiratory Rate
                cal.set(Calendar.HOUR_OF_DAY, 15)
                sampleVitals.add(
                    VitalSign(
                        timestamp = cal.timeInMillis,
                        type = "RESPIRATORY_RATE",
                        primaryValue = (14..16).random().toDouble(),
                        unit = "brpm",
                        status = "Normal",
                        contextNote = "Resting breathing pace"
                    )
                )

                // Temperature
                cal.set(Calendar.HOUR_OF_DAY, 18)
                sampleVitals.add(
                    VitalSign(
                        timestamp = cal.timeInMillis,
                        type = "TEMPERATURE",
                        primaryValue = 98.6 + ((-2..2).random() * 0.1),
                        unit = "°F",
                        status = "Normal",
                        contextNote = "Evening check"
                    )
                )

                // Active Workout Heart Rate
                if (dayOffset % 2 == 0) {
                    cal.set(Calendar.HOUR_OF_DAY, 17)
                    sampleVitals.add(
                        VitalSign(
                            timestamp = cal.timeInMillis,
                            type = "HEART_RATE",
                            primaryValue = (124..138).random().toDouble(),
                            unit = "BPM",
                            status = "Elevated",
                            contextNote = "Post brisk cardio walk"
                        )
                    )
                }
            }
            vitalDao.insertAll(sampleVitals)
        }

        if (mealsCount == 0) {
            val sampleMeals = mutableListOf<NutritionItem>()
            val sampleHydration = mutableListOf<HydrationLog>()

            for (dayOffset in 4 downTo 0) {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -dayOffset)
                }
                val dateStr = dateFormat.format(cal.time)

                // Breakfast
                cal.set(Calendar.HOUR_OF_DAY, 8)
                sampleMeals.add(
                    NutritionItem(
                        date = dateStr,
                        timestamp = cal.timeInMillis,
                        mealType = "Breakfast",
                        foodName = "Steel Cut Oatmeal with Blueberries & Chia Seeds",
                        portionSize = "1 bowl (250g)",
                        calories = 360,
                        proteinGrams = 12.0,
                        carbsGrams = 58.0,
                        fatGrams = 8.5,
                        fiberGrams = 9.0,
                        sugarGrams = 11.0,
                        sodiumMg = 85.0,
                        healthRating = "Superfood"
                    )
                )
                sampleMeals.add(
                    NutritionItem(
                        date = dateStr,
                        timestamp = cal.timeInMillis + 1000,
                        mealType = "Breakfast",
                        foodName = "Greek Yogurt (Non-fat)",
                        portionSize = "1 cup (170g)",
                        calories = 130,
                        proteinGrams = 18.0,
                        carbsGrams = 7.0,
                        fatGrams = 0.5,
                        fiberGrams = 0.0,
                        sugarGrams = 6.0,
                        sodiumMg = 60.0,
                        healthRating = "Healthy"
                    )
                )

                // Lunch
                cal.set(Calendar.HOUR_OF_DAY, 13)
                sampleMeals.add(
                    NutritionItem(
                        date = dateStr,
                        timestamp = cal.timeInMillis,
                        mealType = "Lunch",
                        foodName = "Grilled Chicken Breast with Quinoa & Steamed Greens",
                        portionSize = "1 plate (350g)",
                        calories = 520,
                        proteinGrams = 46.0,
                        carbsGrams = 42.0,
                        fatGrams = 14.0,
                        fiberGrams = 7.5,
                        sugarGrams = 3.0,
                        sodiumMg = 420.0,
                        healthRating = "Superfood"
                    )
                )

                // Snack
                cal.set(Calendar.HOUR_OF_DAY, 16)
                sampleMeals.add(
                    NutritionItem(
                        date = dateStr,
                        timestamp = cal.timeInMillis,
                        mealType = "Snack",
                        foodName = "Crisp Green Apple & Raw Almonds",
                        portionSize = "1 medium apple + 25g nuts",
                        calories = 240,
                        proteinGrams = 6.0,
                        carbsGrams = 26.0,
                        fatGrams = 13.0,
                        fiberGrams = 6.0,
                        sugarGrams = 18.0,
                        sodiumMg = 5.0,
                        healthRating = "Healthy"
                    )
                )

                // Dinner
                cal.set(Calendar.HOUR_OF_DAY, 19)
                sampleMeals.add(
                    NutritionItem(
                        date = dateStr,
                        timestamp = cal.timeInMillis,
                        mealType = "Dinner",
                        foodName = "Atlantic Salmon Fillet with Roasted Sweet Potato & Broccoli",
                        portionSize = "1 serving (380g)",
                        calories = 620,
                        proteinGrams = 42.0,
                        carbsGrams = 48.0,
                        fatGrams = 24.0,
                        fiberGrams = 8.0,
                        sugarGrams = 9.0,
                        sodiumMg = 380.0,
                        healthRating = "Superfood"
                    )
                )

                // Hydration logs
                sampleHydration.add(HydrationLog(date = dateStr, timestamp = cal.timeInMillis - 36000000, amountMl = 500))
                sampleHydration.add(HydrationLog(date = dateStr, timestamp = cal.timeInMillis - 25000000, amountMl = 500))
                sampleHydration.add(HydrationLog(date = dateStr, timestamp = cal.timeInMillis - 15000000, amountMl = 750))
                sampleHydration.add(HydrationLog(date = dateStr, timestamp = cal.timeInMillis - 5000000, amountMl = 500))
            }
            nutritionDao.insertAll(sampleMeals)
            hydrationDao.insertAll(sampleHydration)
        }
    }

    companion object {
        val PRESET_FOODS = listOf(
            PresetFood("Oatmeal with Blueberries", "1 bowl (250g)", 320, 10.0, 54.0, 6.0, 8.0, "Breakfast", "Superfood"),
            PresetFood("Avocado Toast on Sourdough", "2 slices (180g)", 340, 9.0, 36.0, 18.0, 7.0, "Breakfast", "Healthy"),
            PresetFood("Scrambled Eggs (3 eggs) & Spinach", "1 plate", 250, 19.0, 3.0, 17.0, 2.0, "Breakfast", "Lean Protein"),
            PresetFood("Greek Yogurt with Honey & Walnuts", "1 bowl (200g)", 260, 17.0, 21.0, 12.0, 2.0, "Breakfast", "Healthy"),
            PresetFood("Protein Smoothie (Whey, Banana, Berries)", "450ml bottle", 310, 28.0, 38.0, 4.0, 5.0, "Breakfast", "Lean Protein"),
            PresetFood("Grilled Chicken Quinoa Bowl", "1 bowl (350g)", 490, 44.0, 46.0, 12.0, 7.0, "Lunch", "Lean Protein"),
            PresetFood("Tuna Salad Wrap with Greens", "1 wrap (240g)", 380, 32.0, 34.0, 11.0, 5.0, "Lunch", "Healthy"),
            PresetFood("Mediterranean Lentil & Veggie Soup", "1 large bowl (350ml)", 310, 18.0, 48.0, 5.0, 12.0, "Lunch", "Superfood"),
            PresetFood("Turkey Breast Sandwich with Mustard", "1 whole sandwich", 410, 34.0, 42.0, 9.0, 4.0, "Lunch", "Healthy"),
            PresetFood("Brown Rice, Edamame & Tofu Bowl", "1 bowl (320g)", 430, 24.0, 56.0, 12.0, 9.0, "Lunch", "Superfood"),
            PresetFood("Baked Wild Salmon with Asparagus", "1 fillet (300g)", 540, 42.0, 12.0, 28.0, 5.0, "Dinner", "Superfood"),
            PresetFood("Lean Sirloin Steak with Sweet Potato", "1 steak plate (340g)", 580, 48.0, 38.0, 18.0, 6.0, "Dinner", "Lean Protein"),
            PresetFood("Chicken Breast Stir-Fry with Broccoli", "1 plate (380g)", 460, 42.0, 32.0, 14.0, 7.0, "Dinner", "Healthy"),
            PresetFood("Cod Fillet with Roasted Potatoes & Green Beans", "1 plate (350g)", 420, 36.0, 44.0, 8.0, 6.0, "Dinner", "Healthy"),
            PresetFood("Chickpea Coconut Vegetable Curry", "1 bowl (350g)", 480, 16.0, 62.0, 18.0, 11.0, "Dinner", "Healthy"),
            PresetFood("Raw Mixed Almonds & Walnuts", "1 handful (30g)", 190, 6.0, 6.0, 17.0, 3.0, "Snack", "Healthy Fats"),
            PresetFood("Crisp Fuji Apple with Peanut Butter", "1 apple + 1 tbsp", 210, 4.0, 29.0, 8.0, 5.0, "Snack", "Healthy"),
            PresetFood("Cottage Cheese with Sliced Peaches", "1 cup (220g)", 190, 24.0, 16.0, 2.5, 2.0, "Snack", "Lean Protein"),
            PresetFood("Dark Chocolate (85% Cacao)", "3 squares (30g)", 170, 3.0, 13.0, 14.0, 4.0, "Snack", "Treat"),
            PresetFood("Carrot & Cucumber Sticks with Hummus", "1 plate (180g)", 160, 5.0, 19.0, 7.0, 6.0, "Snack", "Superfood")
        )
    }
}
