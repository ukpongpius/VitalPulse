package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NutritionItem
import com.example.data.model.PresetFood
import com.example.data.repository.VitalPulseRepository
import com.example.ui.DailyMacroSummary
import com.example.ui.NutritionSuggestion
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.MacroProgressBar
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralHeart
import com.example.ui.theme.CyanHydration
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun NutritionScreen(
    dailyMeals: List<NutritionItem>,
    macroSummary: DailyMacroSummary,
    nutritionSuggestions: List<NutritionSuggestion>,
    selectedDate: String,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToday: () -> Unit,
    onLogMeal: (mealType: String, foodName: String, portion: String, calories: Int, protein: Double, carbs: Double, fat: Double, fiber: Double, sugar: Double, sodium: Double, rating: String) -> Unit,
    onLogPreset: (PresetFood, String, Double) -> Unit,
    onLogSuggestion: (NutritionSuggestion) -> Unit,
    onDeleteMeal: (Long) -> Unit,
    onQuickWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMealDialog by remember { mutableStateOf(false) }
    var targetMealCategory by remember { mutableStateOf("Breakfast") }

    val breakfastMeals = remember(dailyMeals) { dailyMeals.filter { it.mealType == "Breakfast" } }
    val lunchMeals = remember(dailyMeals) { dailyMeals.filter { it.mealType == "Lunch" } }
    val dinnerMeals = remember(dailyMeals) { dailyMeals.filter { it.mealType == "Dinner" } }
    val snackMeals = remember(dailyMeals) { dailyMeals.filter { it.mealType == "Snack" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("nutrition_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Bar
        item {
            DateSelectorBar(
                selectedDate = selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onResetToday = onResetToday,
                isToday = isToday
            )
        }

        // Daily Energy & Macro Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("macro_energy_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Calorie & Energy Intake",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val remaining = (macroSummary.calorieGoal - macroSummary.totalCalories).coerceAtLeast(0)
                            Text(
                                text = "$remaining kcal remaining of ${macroSummary.calorieGoal} kcal goal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = TealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${macroSummary.totalCalories} kcal",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MacroProgressBar(
                        name = "Protein",
                        current = macroSummary.totalProtein,
                        goal = macroSummary.proteinGoal.toDouble(),
                        unit = "g",
                        barColor = CoralHeart
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MacroProgressBar(
                        name = "Carbohydrates",
                        current = macroSummary.totalCarbs,
                        goal = macroSummary.carbsGoal.toDouble(),
                        unit = "g",
                        barColor = AmberEnergy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MacroProgressBar(
                        name = "Healthy Fats",
                        current = macroSummary.totalFat,
                        goal = macroSummary.fatGoal.toDouble(),
                        unit = "g",
                        barColor = EmeraldAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MacroProgressBar(
                        name = "Dietary Fiber",
                        current = macroSummary.totalFiber,
                        goal = 30.0,
                        unit = "g",
                        barColor = TealPrimary
                    )
                }
            }
        }

        // PERSONALIZED DAILY NUTRITION SUGGESTIONS (Vitals & Wellness targeted)
        if (nutritionSuggestions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("nutrition_suggestions_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AmberEnergy.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AmberEnergy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Suggested Daily Nutrition",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Personalized from your blood pressure & vital signs",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            nutritionSuggestions.forEach { suggestion ->
                                var isLogged by remember { mutableStateOf(false) }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp)),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = TealPrimary.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = suggestion.mealType,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TealPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = EmeraldAccent.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = suggestion.healthTag,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldAccent,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${suggestion.calories} kcal",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = suggestion.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${suggestion.portion} • P: ${suggestion.protein.toInt()}g • C: ${suggestion.carbs.toInt()}g • F: ${suggestion.fat.toInt()}g • Fiber: ${suggestion.fiber.toInt()}g",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "💡 ${suggestion.whyRecommended}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            lineHeight = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onLogSuggestion(suggestion)
                                                isLogged = true
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLogged) EmeraldAccent else TealPrimary
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = if (isLogged) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isLogged) "Added to Today's Meals!" else "+ Log This Recommended Meal",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Hydration Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("hydration_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyanHydration.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalDrink,
                                    contentDescription = null,
                                    tint = CyanHydration,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hydration Level",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${macroSummary.totalWaterMl} / ${macroSummary.waterGoalMl} ml logged",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val waterPercent = ((macroSummary.totalWaterMl.toFloat() / macroSummary.waterGoalMl.coerceAtLeast(1)) * 100).toInt()
                        Text(
                            text = "$waterPercent%",
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanHydration,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onQuickWater(250) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+250ml", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onQuickWater(500) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+500ml", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onQuickWater(750) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanHydration)
                        ) {
                            Text("+750ml", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Breakfast Section
        item {
            MealSectionHeader(
                categoryName = "Breakfast",
                totalCalories = breakfastMeals.sumOf { it.calories },
                onAddClick = {
                    targetMealCategory = "Breakfast"
                    showAddMealDialog = true
                }
            )
        }
        if (breakfastMeals.isEmpty()) {
            item { EmptyMealPlaceholder("No breakfast logged for this date") }
        } else {
            items(breakfastMeals, key = { it.id }) { meal ->
                MealItemCard(meal = meal, onDelete = { onDeleteMeal(meal.id) })
            }
        }

        // Lunch Section
        item {
            MealSectionHeader(
                categoryName = "Lunch",
                totalCalories = lunchMeals.sumOf { it.calories },
                onAddClick = {
                    targetMealCategory = "Lunch"
                    showAddMealDialog = true
                }
            )
        }
        if (lunchMeals.isEmpty()) {
            item { EmptyMealPlaceholder("No lunch logged for this date") }
        } else {
            items(lunchMeals, key = { it.id }) { meal ->
                MealItemCard(meal = meal, onDelete = { onDeleteMeal(meal.id) })
            }
        }

        // Dinner Section
        item {
            MealSectionHeader(
                categoryName = "Dinner",
                totalCalories = dinnerMeals.sumOf { it.calories },
                onAddClick = {
                    targetMealCategory = "Dinner"
                    showAddMealDialog = true
                }
            )
        }
        if (dinnerMeals.isEmpty()) {
            item { EmptyMealPlaceholder("No dinner logged for this date") }
        } else {
            items(dinnerMeals, key = { it.id }) { meal ->
                MealItemCard(meal = meal, onDelete = { onDeleteMeal(meal.id) })
            }
        }

        // Snacks Section
        item {
            MealSectionHeader(
                categoryName = "Snack",
                totalCalories = snackMeals.sumOf { it.calories },
                onAddClick = {
                    targetMealCategory = "Snack"
                    showAddMealDialog = true
                }
            )
        }
        if (snackMeals.isEmpty()) {
            item { EmptyMealPlaceholder("No snacks logged for this date") }
        } else {
            items(snackMeals, key = { it.id }) { meal ->
                MealItemCard(meal = meal, onDelete = { onDeleteMeal(meal.id) })
            }
        }
    }

    if (showAddMealDialog) {
        AddMealDialog(
            defaultCategory = targetMealCategory,
            onDismiss = { showAddMealDialog = false },
            onLogCustom = { name, portion, cal, p, c, f, fiber, rating ->
                onLogMeal(targetMealCategory, name, portion, cal, p, c, f, fiber, 0.0, 0.0, rating)
                showAddMealDialog = false
            },
            onLogPreset = { preset, portionMult ->
                onLogPreset(preset, targetMealCategory, portionMult)
                showAddMealDialog = false
            }
        )
    }
}

@Composable
fun MealSectionHeader(
    categoryName: String,
    totalCalories: Int,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (categoryName) {
                    "Breakfast" -> Icons.Default.EggAlt
                    "Lunch" -> Icons.Default.LunchDining
                    "Dinner" -> Icons.Default.DinnerDining
                    else -> Icons.Default.BakeryDining
                },
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "($totalCalories kcal)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("add_${categoryName.lowercase()}_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Food", fontSize = 12.sp)
        }
    }
}

@Composable
fun MealItemCard(
    meal: NutritionItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("meal_item_${meal.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = meal.foodName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when (meal.healthRating) {
                            "Superfood" -> EmeraldAccent.copy(alpha = 0.15f)
                            "Lean Protein" -> CoralHeart.copy(alpha = 0.15f)
                            else -> TealPrimary.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = meal.healthRating,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (meal.healthRating) {
                                "Superfood" -> EmeraldAccent
                                "Lean Protein" -> CoralHeart
                                else -> TealPrimary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${meal.portionSize} • P: ${meal.proteinGrams.toInt()}g • C: ${meal.carbsGrams.toInt()}g • F: ${meal.fatGrams.toInt()}g • Fiber: ${meal.fiberGrams.toInt()}g",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${meal.calories} kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMealPlaceholder(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
fun AddMealDialog(
    defaultCategory: String,
    onDismiss: () -> Unit,
    onLogCustom: (name: String, portion: String, cal: Int, p: Double, c: Double, f: Double, fiber: Double, rating: String) -> Unit,
    onLogPreset: (PresetFood, Double) -> Unit
) {
    var dialogTab by remember { mutableStateOf(0) } // 0 = Presets, 1 = Custom Food
    var searchQuery by remember { mutableStateOf("") }
    var portionMultiplier by remember { mutableDoubleStateOf(1.0) }

    // Custom fields
    var customName by remember { mutableStateOf("") }
    var customPortion by remember { mutableStateOf("1 serving") }
    var customCalories by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var customFiber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Log $defaultCategory Food", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = dialogTab) {
                    Tab(
                        selected = dialogTab == 0,
                        onClick = { dialogTab = 0 },
                        text = { Text("Healthy Presets", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = dialogTab == 1,
                        onClick = { dialogTab = 1 },
                        text = { Text("Custom Meal", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (dialogTab == 0) {
                    // Presets
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search healthy foods") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Portion: ${(portionMultiplier * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = portionMultiplier.toFloat(),
                        onValueChange = { portionMultiplier = it.toDouble() },
                        valueRange = 0.5f..2.5f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredPresets = remember(searchQuery) {
                        VitalPulseRepository.PRESET_FOODS.filter {
                            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.tag.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(filteredPresets) { food ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onLogPreset(food, portionMultiplier) },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = food.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(
                                            text = "${food.defaultPortion} • ${(food.calories * portionMultiplier).toInt()} kcal",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Select",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom Entry
                    LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("Food / Meal Name *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = customPortion,
                                onValueChange = { customPortion = it },
                                label = { Text("Portion Size (e.g. 1 bowl, 200g)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customCalories,
                                    onValueChange = { customCalories = it },
                                    label = { Text("Calories *") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customProtein,
                                    onValueChange = { customProtein = it },
                                    label = { Text("Protein (g)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customCarbs,
                                    onValueChange = { customCarbs = it },
                                    label = { Text("Carbs (g)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customFat,
                                    onValueChange = { customFat = it },
                                    label = { Text("Fat (g)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = customFiber,
                                onValueChange = { customFiber = it },
                                label = { Text("Fiber (g)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (dialogTab == 1) {
                Button(
                    onClick = {
                        val cal = customCalories.toIntOrNull() ?: 0
                        val p = customProtein.toDoubleOrNull() ?: 0.0
                        val c = customCarbs.toDoubleOrNull() ?: 0.0
                        val f = customFat.toDoubleOrNull() ?: 0.0
                        val fiber = customFiber.toDoubleOrNull() ?: 0.0
                        if (customName.isNotBlank() && cal > 0) {
                            onLogCustom(customName, customPortion, cal, p, c, f, fiber, "Healthy")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Add Custom Food")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
