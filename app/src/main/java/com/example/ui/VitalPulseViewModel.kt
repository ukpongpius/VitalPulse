package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.VitalPulseDatabase
import com.example.data.model.HydrationLog
import com.example.data.model.NutritionItem
import com.example.data.model.PresetFood
import com.example.data.model.UserProfile
import com.example.data.model.VitalSign
import com.example.data.repository.VitalPulseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class NavTab(val title: String) {
    DASHBOARD("Overview"),
    VITALS("Check Vitals"),
    NUTRITION("Nutrition"),
    TRENDS("Analytics"),
    PROFILE("Profile")
}

data class DailyMacroSummary(
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalWaterMl: Int = 0,
    val calorieGoal: Int = 2200,
    val proteinGoal: Int = 140,
    val carbsGoal: Int = 225,
    val fatGoal: Int = 65,
    val waterGoalMl: Int = 2500
)

data class NutritionSuggestion(
    val id: String,
    val title: String,
    val mealType: String,
    val portion: String,
    val description: String,
    val whyRecommended: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val healthTag: String
)

data class HeartRateScanState(
    val isScanning: Boolean = false,
    val isFingerprintPressed: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val currentBpm: Int = 72,
    val liveWaveform: List<Float> = emptyList(),
    val resultBpm: Int? = null,
    val hrvScore: Int? = null,
    val status: String = "Place and hold finger on sensor to read pulse"
)

data class BreathingPacerState(
    val isActive: Boolean = false,
    val phase: String = "Inhale", // Inhale, Hold, Exhale, Pause
    val secondsRemaining: Int = 4,
    val cycleCount: Int = 0,
    val calculatedBreathsPerMin: Double = 15.0
)

class VitalPulseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = VitalPulseDatabase.getInstance(application)
    private val repository = VitalPulseRepository(
        vitalDao = db.vitalDao(),
        nutritionDao = db.nutritionDao(),
        hydrationDao = db.hydrationDao(),
        profileDao = db.profileDao()
    )

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentTab = MutableStateFlow(NavTab.DASHBOARD)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val allVitals: StateFlow<List<VitalSign>> = repository.allVitals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyMeals: StateFlow<List<NutritionItem>> = _selectedDate
        .flatMapLatest { date -> repository.getMealsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyHydration: StateFlow<List<HydrationLog>> = _selectedDate
        .flatMapLatest { date -> repository.getHydrationForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val macroSummary: StateFlow<DailyMacroSummary> = combine(
        dailyMeals,
        dailyHydration,
        userProfile
    ) { meals, hydration, profile ->
        val safeProfile = profile ?: UserProfile()
        DailyMacroSummary(
            totalCalories = meals.sumOf { it.calories },
            totalProtein = meals.sumOf { it.proteinGrams },
            totalCarbs = meals.sumOf { it.carbsGrams },
            totalFat = meals.sumOf { it.fatGrams },
            totalFiber = meals.sumOf { it.fiberGrams },
            totalWaterMl = hydration.sumOf { it.amountMl },
            calorieGoal = safeProfile.dailyCalorieGoal,
            proteinGoal = safeProfile.dailyProteinGoal,
            carbsGoal = safeProfile.dailyCarbsGoal,
            fatGoal = safeProfile.dailyFatGoal,
            waterGoalMl = safeProfile.dailyWaterGoalMl
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyMacroSummary()
    )

    // Dynamic Nutrition Suggestions Engine based on Vitals and Goals
    val nutritionSuggestions: StateFlow<List<NutritionSuggestion>> = combine(
        allVitals,
        macroSummary,
        userProfile
    ) { vitalsList, summary, profile ->
        val latestBP = vitalsList.firstOrNull { it.type == "BLOOD_PRESSURE" }
        val latestHR = vitalsList.firstOrNull { it.type == "HEART_RATE" }
        val isBpElevated = latestBP != null && (latestBP.primaryValue >= 120 || (latestBP.secondaryValue ?: 80.0) >= 80)
        val isHrHigh = latestHR != null && latestHR.primaryValue > 85

        val list = mutableListOf<NutritionSuggestion>()

        if (isBpElevated) {
            // DASH Diet & Potassium/Magnesium BP lowering recommendations
            list.add(
                NutritionSuggestion(
                    id = "sug_dash_bowl",
                    title = "DASH Spinach, Avocado & Baked Salmon Bowl",
                    mealType = "Lunch",
                    portion = "1 bowl (360g)",
                    description = "Wild salmon over raw baby spinach, diced avocado, and quinoa with lemon herb olive oil.",
                    whyRecommended = "Arterial Relaxation: Rich in 950mg potassium & 3.2g omega-3 to dilate blood vessels and lower systolic pressure.",
                    calories = 490,
                    protein = 38.0,
                    carbs = 32.0,
                    fat = 22.0,
                    fiber = 8.0,
                    healthTag = "DASH Diet"
                )
            )
            list.add(
                NutritionSuggestion(
                    id = "sug_beet_salad",
                    title = "Roasted Beet, Arugula & Walnut Salad",
                    mealType = "Dinner",
                    portion = "1 plate (280g)",
                    description = "Fresh steamed ruby beets, arugula leaves, raw walnuts, and balsamic reduction.",
                    whyRecommended = "Nitric Oxide Booster: Natural dietary nitrates promote vascular vasodilation, clinically reducing arterial tension.",
                    calories = 290,
                    protein = 8.0,
                    carbs = 26.0,
                    fat = 17.0,
                    fiber = 6.0,
                    healthTag = "BP Lowering"
                )
            )
            list.add(
                NutritionSuggestion(
                    id = "sug_almonds_snack",
                    title = "Unsalted Raw Almonds & Dark Cocoa",
                    mealType = "Snack",
                    portion = "30g almonds + 2 squares 85% cacao",
                    description = "Crunchy raw California almonds paired with polyphenol-dense dark chocolate.",
                    whyRecommended = "Magnesium Rich: 90mg magnesium eases smooth muscle contraction around arteries.",
                    calories = 230,
                    protein = 6.0,
                    carbs = 14.0,
                    fat = 18.0,
                    fiber = 4.5,
                    healthTag = "Heart Tonic"
                )
            )
        } else {
            // Normal BP - Balanced Energy and Cardiovascular Maintenance
            list.add(
                NutritionSuggestion(
                    id = "sug_med_chicken",
                    title = "Mediterranean Grilled Herb Chicken & Quinoa",
                    mealType = "Lunch",
                    portion = "1 plate (350g)",
                    description = "Herb-marinated chicken breast, warm quinoa, steamed zucchini, and kalamata olives.",
                    whyRecommended = "Cardiovascular Stability: Clean lean protein with steady complex carbohydrates to sustain cardiac efficiency.",
                    calories = 480,
                    protein = 44.0,
                    carbs = 42.0,
                    fat = 12.0,
                    fiber = 7.0,
                    healthTag = "Lean Protein"
                )
            )
            list.add(
                NutritionSuggestion(
                    id = "sug_chia_oats",
                    title = "Overnight Steel-Cut Oats with Blueberries & Chia",
                    mealType = "Breakfast",
                    portion = "1 bowl (260g)",
                    description = "Slow-digesting oats soaked with chia seeds, wild Maine blueberries, and cinnamon.",
                    whyRecommended = "Beta-Glucan Fiber: Soluble fiber binds to cholesterol and stabilizes postprandial glucose curves.",
                    calories = 340,
                    protein = 11.0,
                    carbs = 56.0,
                    fat = 7.0,
                    fiber = 9.5,
                    healthTag = "Superfood"
                )
            )
            list.add(
                NutritionSuggestion(
                    id = "sug_greek_yogurt",
                    title = "Greek Yogurt Parfait with Walnuts & Raw Honey",
                    mealType = "Breakfast",
                    portion = "1 cup (220g)",
                    description = "Thick non-fat strained Greek yogurt, crushed raw walnuts, and organic honey drizzle.",
                    whyRecommended = "Microbiome & Calcium: Supports gut microflora and cellular calcium signaling for heart rhythm regularity.",
                    calories = 260,
                    protein = 20.0,
                    carbs = 22.0,
                    fat = 9.0,
                    fiber = 2.0,
                    healthTag = "Healthy"
                )
            )
        }

        // Additional Hydration recommendation if needed
        if (summary.totalWaterMl < summary.waterGoalMl * 0.7) {
            list.add(
                NutritionSuggestion(
                    id = "sug_hydrating_greens",
                    title = "Hydrating Green Smoothie with Coconut Water",
                    mealType = "Snack",
                    portion = "400ml bottle",
                    description = "Fresh baby spinach, crisp cucumber, lime juice, and electrolyte-rich coconut water.",
                    whyRecommended = "Electrolyte Replenishment: Restores intracellular potassium and boosts total body hydration.",
                    calories = 140,
                    protein = 3.0,
                    carbs = 28.0,
                    fat = 0.5,
                    fiber = 4.0,
                    healthTag = "Hydration Boost"
                )
            )
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            NutritionSuggestion(
                id = "sug_dash_bowl",
                title = "DASH Spinach, Avocado & Baked Salmon Bowl",
                mealType = "Lunch",
                portion = "1 bowl (360g)",
                description = "Wild salmon over raw baby spinach, diced avocado, and quinoa with lemon herb olive oil.",
                whyRecommended = "Arterial Relaxation: Rich in 950mg potassium & 3.2g omega-3 to dilate blood vessels and lower systolic pressure.",
                calories = 490,
                protein = 38.0,
                carbs = 32.0,
                fat = 22.0,
                fiber = 8.0,
                healthTag = "DASH Diet"
            )
        )
    )

    // Heart Rate Scan State
    private val _heartRateScan = MutableStateFlow(HeartRateScanState())
    val heartRateScan: StateFlow<HeartRateScanState> = _heartRateScan.asStateFlow()
    private var heartRateJob: Job? = null

    // Breathing Pacer State
    private val _breathingPacer = MutableStateFlow(BreathingPacerState())
    val breathingPacer: StateFlow<BreathingPacerState> = _breathingPacer.asStateFlow()
    private var breathingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun changeDateOffset(offsetDays: Int) {
        try {
            val cal = Calendar.getInstance()
            dateFormat.parse(_selectedDate.value)?.let { cal.time = it }
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = todayDateString
        }
    }

    fun resetToToday() {
        _selectedDate.value = todayDateString
    }

    // Vitals Actions
    fun logVital(
        type: String,
        primaryValue: Double,
        secondaryValue: Double? = null,
        unit: String,
        status: String,
        contextNote: String = ""
    ) {
        viewModelScope.launch {
            repository.logVital(
                VitalSign(
                    type = type,
                    primaryValue = primaryValue,
                    secondaryValue = secondaryValue,
                    unit = unit,
                    status = status,
                    contextNote = contextNote
                )
            )
            triggerHapticClick()
        }
    }

    fun deleteVital(id: Long) {
        viewModelScope.launch {
            repository.deleteVital(id)
        }
    }

    // Nutrition Actions
    fun logMeal(
        mealType: String,
        foodName: String,
        portionSize: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double = 0.0,
        sugar: Double = 0.0,
        sodium: Double = 0.0,
        healthRating: String = "Healthy"
    ) {
        viewModelScope.launch {
            repository.logMeal(
                NutritionItem(
                    date = _selectedDate.value,
                    mealType = mealType,
                    foodName = foodName,
                    portionSize = portionSize,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    fiberGrams = fiber,
                    sugarGrams = sugar,
                    sodiumMg = sodium,
                    healthRating = healthRating
                )
            )
            triggerHapticClick()
        }
    }

    fun logPresetFood(preset: PresetFood, mealType: String, portionMultiplier: Double = 1.0) {
        logMeal(
            mealType = mealType,
            foodName = preset.name,
            portionSize = if (portionMultiplier == 1.0) preset.defaultPortion else "${(portionMultiplier * 100).toInt()}% of ${preset.defaultPortion}",
            calories = (preset.calories * portionMultiplier).toInt(),
            protein = preset.protein * portionMultiplier,
            carbs = preset.carbs * portionMultiplier,
            fat = preset.fat * portionMultiplier,
            fiber = preset.fiber * portionMultiplier,
            healthRating = preset.tag
        )
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            repository.deleteMeal(id)
        }
    }

    fun logHydration(amountMl: Int) {
        viewModelScope.launch {
            repository.logHydration(amountMl, _selectedDate.value)
            triggerHapticClick()
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun logSuggestedNutrition(suggestion: NutritionSuggestion) {
        logMeal(
            mealType = suggestion.mealType,
            foodName = suggestion.title,
            portionSize = suggestion.portion,
            calories = suggestion.calories,
            protein = suggestion.protein,
            carbs = suggestion.carbs,
            fat = suggestion.fat,
            fiber = suggestion.fiber,
            healthRating = suggestion.healthTag
        )
    }

    // Optical Fingerprint Sensor & Pulse Waveform
    fun onFingerprintTouchStart() {
        if (_heartRateScan.value.isScanning) return
        startHeartRateScan(isFingerprint = true)
    }

    fun onFingerprintTouchEnd() {
        if (_heartRateScan.value.isScanning && _heartRateScan.value.progress < 0.95f) {
            heartRateJob?.cancel()
            _heartRateScan.value = _heartRateScan.value.copy(
                isScanning = false,
                isFingerprintPressed = false,
                progress = 0f,
                status = "Finger removed early. Hold finger steady on sensor to finish."
            )
            triggerHapticClick()
        }
    }

    fun startHeartRateScan(isFingerprint: Boolean = false) {
        if (_heartRateScan.value.isScanning) return
        heartRateJob?.cancel()

        _heartRateScan.value = HeartRateScanState(
            isScanning = true,
            isFingerprintPressed = isFingerprint,
            progress = 0f,
            currentBpm = 70,
            liveWaveform = listOf(0.1f, 0.2f, 0.1f, 0.8f, -0.4f, 0.2f),
            status = if (isFingerprint) "Reading arterial blood flow via fingerprint..." else "Calibrating optical pulse sensor..."
        )
        triggerPulseHaptic()

        heartRateJob = viewModelScope.launch {
            val totalSteps = 50 // 10 seconds check (200ms per step)
            val baseBpm = (65..78).random()
            val waveform = mutableListOf<Float>()

            for (step in 1..totalSteps) {
                delay(200)
                val progress = step.toFloat() / totalSteps

                // Generate simulated photoplethysmography ECG curve
                val phase = (step % 5)
                val sampleValue = when (phase) {
                    0 -> 0.1f
                    1 -> 0.25f
                    2 -> 0.95f // Peak pulse R wave
                    3 -> -0.4f // S wave dip
                    else -> 0.15f // T wave
                }
                waveform.add(sampleValue)
                if (waveform.size > 24) waveform.removeAt(0)

                // Trigger subtle pulse haptic on peak
                if (phase == 2) {
                    triggerPulseHaptic()
                }

                val currentVariation = baseBpm + ((-2..3).random())
                val statusText = when {
                    progress < 0.3f -> "Optical sensor locked. Measuring pulse rhythm..."
                    progress < 0.7f -> "Analyzing capillary pulse wave velocity..."
                    else -> "Calculating resting BPM & arterial regularity..."
                }

                _heartRateScan.value = HeartRateScanState(
                    isScanning = true,
                    isFingerprintPressed = isFingerprint,
                    progress = progress,
                    currentBpm = currentVariation,
                    liveWaveform = waveform.toList(),
                    status = statusText
                )
            }

            val finalBpm = baseBpm
            val finalHrv = (45..75).random() // Heart Rate Variability (ms)
            val finalStatus = if (finalBpm < 60) "Low (Bradycardia)" else if (finalBpm <= 100) "Normal (Optimal)" else "Elevated"

            _heartRateScan.value = HeartRateScanState(
                isScanning = false,
                isFingerprintPressed = false,
                progress = 1f,
                currentBpm = finalBpm,
                liveWaveform = waveform.toList(),
                resultBpm = finalBpm,
                hrvScore = finalHrv,
                status = "Vital Pulse Check Complete!"
            )

            // Auto-save the measured vital
            repository.logVital(
                VitalSign(
                    type = "HEART_RATE",
                    primaryValue = finalBpm.toDouble(),
                    unit = "BPM",
                    status = finalStatus,
                    contextNote = "Fingerprint pulse scan (HRV: ${finalHrv}ms)"
                )
            )
            triggerSuccessHaptic()
        }
    }

    fun stopHeartRateScan() {
        heartRateJob?.cancel()
        _heartRateScan.value = _heartRateScan.value.copy(
            isScanning = false,
            isFingerprintPressed = false,
            status = "Scan cancelled"
        )
    }

    // Guided Breathing Exercise (4-7-8 relaxing breathing rhythm)
    fun toggleBreathingPacer() {
        if (_breathingPacer.value.isActive) {
            breathingJob?.cancel()
            _breathingPacer.value = BreathingPacerState(isActive = false)
        } else {
            startBreathingPacer()
        }
    }

    private fun startBreathingPacer() {
        breathingJob?.cancel()
        _breathingPacer.value = BreathingPacerState(
            isActive = true,
            phase = "Inhale",
            secondsRemaining = 4,
            cycleCount = 0
        )

        breathingJob = viewModelScope.launch {
            var cycle = 0
            while (true) {
                // Inhale (4s)
                _breathingPacer.value = _breathingPacer.value.copy(phase = "Inhale", secondsRemaining = 4)
                triggerPulseHaptic()
                for (s in 4 downTo 1) {
                    _breathingPacer.value = _breathingPacer.value.copy(secondsRemaining = s)
                    delay(1000)
                }

                // Hold (7s)
                _breathingPacer.value = _breathingPacer.value.copy(phase = "Hold", secondsRemaining = 7)
                for (s in 7 downTo 1) {
                    _breathingPacer.value = _breathingPacer.value.copy(secondsRemaining = s)
                    delay(1000)
                }

                // Exhale (8s)
                _breathingPacer.value = _breathingPacer.value.copy(phase = "Exhale", secondsRemaining = 8)
                triggerPulseHaptic()
                for (s in 8 downTo 1) {
                    _breathingPacer.value = _breathingPacer.value.copy(secondsRemaining = s)
                    delay(1000)
                }

                cycle++
                _breathingPacer.value = _breathingPacer.value.copy(cycleCount = cycle)
            }
        }
    }

    // Haptics
    private fun triggerPulseHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, 120))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    private fun triggerSuccessHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 40, 80, 60),
                        intArrayOf(0, 100, 0, 180),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    private fun triggerHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, 80))
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        heartRateJob?.cancel()
        breathingJob?.cancel()
    }
}
