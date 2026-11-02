package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VitalPulseDatabase
import com.example.data.model.HydrationLog
import com.example.data.model.NutritionItem
import com.example.data.model.UserProfile
import com.example.data.model.VitalSign
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VitalPulseDatabaseTest {

    private lateinit var db: VitalPulseDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalPulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveVitalSign() = runBlocking {
        val vital = VitalSign(
            type = "HEART_RATE",
            primaryValue = 72.0,
            unit = "BPM",
            status = "Normal",
            contextNote = "Resting"
        )
        val id = db.vitalDao().insertVital(vital)
        assertEquals(1L, id)

        val vitals = db.vitalDao().getAllVitals().first()
        assertEquals(1, vitals.size)
        assertEquals("HEART_RATE", vitals[0].type)
        assertEquals(72.0, vitals[0].primaryValue, 0.01)
    }

    @Test
    fun testInsertAndQueryMealsByDate() = runBlocking {
        val dateToday = "2026-10-30"
        val dateYesterday = "2026-10-29"

        val meal1 = NutritionItem(
            date = dateToday,
            mealType = "Breakfast",
            foodName = "Oatmeal",
            calories = 300,
            proteinGrams = 10.0,
            carbsGrams = 50.0,
            fatGrams = 5.0
        )
        val meal2 = NutritionItem(
            date = dateYesterday,
            mealType = "Dinner",
            foodName = "Steak",
            calories = 600,
            proteinGrams = 50.0,
            carbsGrams = 0.0,
            fatGrams = 20.0
        )

        db.nutritionDao().insertMeal(meal1)
        db.nutritionDao().insertMeal(meal2)

        val todayMeals = db.nutritionDao().getMealsForDate(dateToday).first()
        assertEquals(1, todayMeals.size)
        assertEquals("Oatmeal", todayMeals[0].foodName)

        val yesterdayMeals = db.nutritionDao().getMealsForDate(dateYesterday).first()
        assertEquals(1, yesterdayMeals.size)
        assertEquals("Steak", yesterdayMeals[0].foodName)
    }

    @Test
    fun testHydrationAndProfilePersistence() = runBlocking {
        val date = "2026-10-30"
        db.hydrationDao().insertHydration(HydrationLog(date = date, amountMl = 500))
        db.hydrationDao().insertHydration(HydrationLog(date = date, amountMl = 250))

        val logs = db.hydrationDao().getHydrationForDate(date).first()
        assertEquals(2, logs.size)
        assertEquals(750, logs.sumOf { it.amountMl })

        val profile = UserProfile(
            name = "Alex",
            age = 30,
            weightKg = 75.0,
            heightCm = 180.0,
            dailyCalorieGoal = 2300
        )
        db.profileDao().insertOrUpdateProfile(profile)

        val loadedProfile = db.profileDao().getProfile().first()
        assertNotNull(loadedProfile)
        assertEquals("Alex", loadedProfile?.name)
        assertEquals(2300, loadedProfile?.dailyCalorieGoal)
    }

    @Test
    fun testDeleteVitalAndMeal() = runBlocking {
        val vitalId = db.vitalDao().insertVital(
            VitalSign(type = "SPO2", primaryValue = 99.0, unit = "%", status = "Normal")
        )
        assertEquals(1, db.vitalDao().getAllVitals().first().size)

        db.vitalDao().deleteVitalById(vitalId)
        assertEquals(0, db.vitalDao().getAllVitals().first().size)
    }
}
