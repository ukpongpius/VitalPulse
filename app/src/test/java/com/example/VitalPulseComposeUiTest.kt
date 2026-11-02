package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.ui.theme.VitalPulseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VitalPulseComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppRootAndNavigationDisplayed() {
        composeTestRule.setContent {
            VitalPulseTheme {
                VitalPulseApp()
            }
        }

        composeTestRule.waitForIdle()

        // Verify root Scaffold and Bottom Nav Bar are present
        composeTestRule.onNodeWithTag("vital_pulse_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()

        // Verify Dashboard components in view
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("date_selector_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hero_banner_card").assertIsDisplayed()

        // Scroll to nutrition summary card in LazyColumn
        composeTestRule.onNodeWithTag("dashboard_screen")
            .performScrollToNode(hasTestTag("nutrition_summary_card"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nutrition_summary_card").assertIsDisplayed()
    }

    @Test
    fun testTabNavigationSwitching() {
        composeTestRule.setContent {
            VitalPulseTheme {
                VitalPulseApp()
            }
        }

        composeTestRule.waitForIdle()

        // Click on Vitals Tab
        composeTestRule.onNodeWithTag("nav_tab_vitals").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("vitals_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("heart_rate_checker_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fingerprint_sensor_pad").assertIsDisplayed()

        // Click on Nutrition Tab
        composeTestRule.onNodeWithTag("nav_tab_nutrition").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nutrition_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("macro_energy_card").assertIsDisplayed()

        // Click on Analytics Tab
        composeTestRule.onNodeWithTag("nav_tab_analytics").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("analytics_screen").assertIsDisplayed()

        // Click on Profile Tab
        composeTestRule.onNodeWithTag("nav_tab_profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()

        // Return to Overview
        composeTestRule.onNodeWithTag("nav_tab_overview").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
    }

    @Test
    fun testBloodPressureNormalAndAbnormalModule() {
        composeTestRule.setContent {
            VitalPulseTheme {
                VitalPulseApp()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to Vitals
        composeTestRule.onNodeWithTag("nav_tab_vitals").performClick()
        composeTestRule.waitForIdle()

        // Switch to Blood Pressure Tab (index 1)
        composeTestRule.onNodeWithTag("vital_tab_1").performClick()
        composeTestRule.waitForIdle()

        // Blood pressure card should be displayed
        composeTestRule.onNodeWithTag("blood_pressure_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("systolic_slider").assertExists()
        composeTestRule.onNodeWithTag("diastolic_slider").assertExists()
        composeTestRule.onNodeWithTag("log_blood_pressure_button").assertExists()

        // Verify normal / not normal status banner exists
        composeTestRule.onNodeWithTag("bp_status_banner").assertExists()
        composeTestRule.onNodeWithText("BLOOD PRESSURE IS NOT NORMAL", substring = true).assertExists()
    }

    @Test
    fun testNutritionSuggestionsDisplayed() {
        composeTestRule.setContent {
            VitalPulseTheme {
                VitalPulseApp()
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to Nutrition
        composeTestRule.onNodeWithTag("nav_tab_nutrition").performClick()
        composeTestRule.waitForIdle()

        // Verify Nutrition Screen and suggestions card
        composeTestRule.onNodeWithTag("nutrition_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nutrition_screen")
            .performScrollToNode(hasTestTag("nutrition_suggestions_card"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nutrition_suggestions_card").assertIsDisplayed()
    }
}
