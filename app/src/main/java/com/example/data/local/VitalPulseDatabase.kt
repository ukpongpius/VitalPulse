package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.HydrationLog
import com.example.data.model.NutritionItem
import com.example.data.model.UserProfile
import com.example.data.model.VitalSign

@Database(
    entities = [
        VitalSign::class,
        NutritionItem::class,
        HydrationLog::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VitalPulseDatabase : RoomDatabase() {
    abstract fun vitalDao(): VitalDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: VitalPulseDatabase? = null

        fun getInstance(context: Context): VitalPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VitalPulseDatabase::class.java,
                    "vital_pulse.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
