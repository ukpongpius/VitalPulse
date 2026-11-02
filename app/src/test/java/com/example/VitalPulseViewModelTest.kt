package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.NavTab
import com.example.ui.VitalPulseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VitalPulseViewModelTest {

    private lateinit var viewModel: VitalPulseViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = VitalPulseViewModel(application)
    }

    @Test
    fun testNavigationTabs() {
        assertEquals(NavTab.DASHBOARD, viewModel.currentTab.value)

        viewModel.setTab(NavTab.VITALS)
        assertEquals(NavTab.VITALS, viewModel.currentTab.value)

        viewModel.setTab(NavTab.NUTRITION)
        assertEquals(NavTab.NUTRITION, viewModel.currentTab.value)

        viewModel.setTab(NavTab.TRENDS)
        assertEquals(NavTab.TRENDS, viewModel.currentTab.value)

        viewModel.setTab(NavTab.PROFILE)
        assertEquals(NavTab.PROFILE, viewModel.currentTab.value)
    }

    @Test
    fun testDateNavigation() {
        val today = viewModel.todayDateString
        assertEquals(today, viewModel.selectedDate.value)

        viewModel.changeDateOffset(-1)
        assertFalse(viewModel.selectedDate.value == today)

        viewModel.resetToToday()
        assertEquals(today, viewModel.selectedDate.value)
    }

    @Test
    fun testLogVitalAndHydration() = runTest {
        viewModel.logVital(
            type = "HEART_RATE",
            primaryValue = 68.0,
            secondaryValue = null,
            unit = "BPM",
            status = "Normal",
            contextNote = "Test resting"
        )
        viewModel.logHydration(500)
        advanceUntilIdle()

        assertNotNull(viewModel.dailyHydration.value)
    }
}
