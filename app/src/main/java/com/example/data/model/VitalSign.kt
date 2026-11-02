package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vital_signs")
data class VitalSign(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // HEART_RATE, BLOOD_PRESSURE, SPO2, RESPIRATORY_RATE, TEMPERATURE
    val primaryValue: Double,
    val secondaryValue: Double? = null, // e.g. diastolic for blood pressure
    val unit: String,
    val status: String, // Normal, Elevated, Low, Warning
    val contextNote: String = ""
)

enum class VitalType(val displayName: String, val defaultUnit: String) {
    HEART_RATE("Heart Rate", "BPM"),
    BLOOD_PRESSURE("Blood Pressure", "mmHg"),
    SPO2("Blood Oxygen", "%"),
    RESPIRATORY_RATE("Respiratory Rate", "brpm"),
    TEMPERATURE("Body Temperature", "°F")
}
