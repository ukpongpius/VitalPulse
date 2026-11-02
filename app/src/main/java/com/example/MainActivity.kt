package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavTab
import com.example.ui.VitalPulseViewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VitalsScreen
import com.example.ui.theme.CoralHeart
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.VitalPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalPulseTheme {
                VitalPulseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalPulseApp(
    viewModel: VitalPulseViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allVitals by viewModel.allVitals.collectAsStateWithLifecycle()
    val dailyMeals by viewModel.dailyMeals.collectAsStateWithLifecycle()
    val macroSummary by viewModel.macroSummary.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val heartRateScan by viewModel.heartRateScan.collectAsStateWithLifecycle()
    val breathingPacer by viewModel.breathingPacer.collectAsStateWithLifecycle()
    val nutritionSuggestions by viewModel.nutritionSuggestions.collectAsStateWithLifecycle()

    val isToday = selectedDate == viewModel.todayDateString

    // Handle Back Press to return to Dashboard if in other tabs
    BackHandler(enabled = currentTab != NavTab.DASHBOARD) {
        viewModel.setTab(NavTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vital_pulse_root"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "VitalPulse",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    Surface(
                        color = CoralHeart.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Pulse Heart",
                            tint = CoralHeart,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavTab.DASHBOARD,
                    onClick = { viewModel.setTab(NavTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Overview"
                        )
                    },
                    label = { Text("Overview", fontSize = 11.sp, fontWeight = if (currentTab == NavTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_overview")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.VITALS,
                    onClick = { viewModel.setTab(NavTab.VITALS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.VITALS) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Check Vitals"
                        )
                    },
                    label = { Text("Vitals", fontSize = 11.sp, fontWeight = if (currentTab == NavTab.VITALS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoralHeart,
                        indicatorColor = CoralHeart.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_vitals")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.NUTRITION,
                    onClick = { viewModel.setTab(NavTab.NUTRITION) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.NUTRITION) Icons.Default.Restaurant else Icons.Outlined.Restaurant,
                            contentDescription = "Nutrition"
                        )
                    },
                    label = { Text("Nutrition", fontSize = 11.sp, fontWeight = if (currentTab == NavTab.NUTRITION) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_nutrition")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.TRENDS,
                    onClick = { viewModel.setTab(NavTab.TRENDS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.TRENDS) Icons.Default.Insights else Icons.Outlined.Insights,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Analytics", fontSize = 11.sp, fontWeight = if (currentTab == NavTab.TRENDS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_analytics")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.PROFILE,
                    onClick = { viewModel.setTab(NavTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavTab.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = if (currentTab == NavTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                NavTab.DASHBOARD -> DashboardScreen(
                    userProfile = userProfile,
                    vitals = allVitals,
                    macroSummary = macroSummary,
                    selectedDate = selectedDate,
                    isToday = isToday,
                    onPreviousDay = { viewModel.changeDateOffset(-1) },
                    onNextDay = { viewModel.changeDateOffset(1) },
                    onResetToday = { viewModel.resetToToday() },
                    onNavigateTab = { viewModel.setTab(it) },
                    onQuickWater = { viewModel.logHydration(it) }
                )
                NavTab.VITALS -> VitalsScreen(
                    vitals = allVitals,
                    heartRateScan = heartRateScan,
                    breathingPacer = breathingPacer,
                    onStartHeartRateScan = { viewModel.startHeartRateScan(false) },
                    onStopHeartRateScan = { viewModel.stopHeartRateScan() },
                    onFingerprintTouchStart = { viewModel.onFingerprintTouchStart() },
                    onFingerprintTouchEnd = { viewModel.onFingerprintTouchEnd() },
                    onToggleBreathingPacer = { viewModel.toggleBreathingPacer() },
                    onLogVital = { type, pVal, sVal, unit, status, note ->
                        viewModel.logVital(type, pVal, sVal, unit, status, note)
                    },
                    onDeleteVital = { viewModel.deleteVital(it) }
                )
                NavTab.NUTRITION -> NutritionScreen(
                    dailyMeals = dailyMeals,
                    macroSummary = macroSummary,
                    nutritionSuggestions = nutritionSuggestions,
                    selectedDate = selectedDate,
                    isToday = isToday,
                    onPreviousDay = { viewModel.changeDateOffset(-1) },
                    onNextDay = { viewModel.changeDateOffset(1) },
                    onResetToday = { viewModel.resetToToday() },
                    onLogMeal = { mealType, name, portion, cal, p, c, f, fiber, sugar, sodium, rating ->
                        viewModel.logMeal(mealType, name, portion, cal, p, c, f, fiber, sugar, sodium, rating)
                    },
                    onLogPreset = { preset, category, multiplier ->
                        viewModel.logPresetFood(preset, category, multiplier)
                    },
                    onLogSuggestion = { viewModel.logSuggestedNutrition(it) },
                    onDeleteMeal = { viewModel.deleteMeal(it) },
                    onQuickWater = { viewModel.logHydration(it) }
                )
                NavTab.TRENDS -> AnalyticsScreen(
                    vitals = allVitals,
                    allMeals = dailyMeals,
                    userProfile = userProfile
                )
                NavTab.PROFILE -> ProfileScreen(
                    profile = userProfile,
                    onSaveProfile = { viewModel.updateProfile(it) }
                )
            }
        }
    }
}
