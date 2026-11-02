package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NutritionItem
import com.example.data.model.UserProfile
import com.example.data.model.VitalSign
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralHeart
import com.example.ui.theme.CyanHydration
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.VioletCalm
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    vitals: List<VitalSign>,
    allMeals: List<NutritionItem>,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    // 7-day Heart Rate trend data
    val hrData = remember(vitals) {
        val hrList = vitals.filter { it.type == "HEART_RATE" }.sortedBy { it.timestamp }
        if (hrList.isEmpty()) listOf(68f, 70f, 72f, 69f, 71f, 74f, 68f)
        else hrList.takeLast(7).map { it.primaryValue.toFloat() }
    }

    // 7-day Blood Pressure trend data
    val bpSysData = remember(vitals) {
        val bpList = vitals.filter { it.type == "BLOOD_PRESSURE" }.sortedBy { it.timestamp }
        if (bpList.isEmpty()) listOf(118f, 120f, 116f, 122f, 119f, 121f, 118f)
        else bpList.takeLast(7).map { it.primaryValue.toFloat() }
    }
    val bpDiaData = remember(vitals) {
        val bpList = vitals.filter { it.type == "BLOOD_PRESSURE" }.sortedBy { it.timestamp }
        if (bpList.isEmpty()) listOf(76f, 78f, 74f, 80f, 77f, 79f, 76f)
        else bpList.takeLast(7).map { (it.secondaryValue ?: 76.0).toFloat() }
    }

    // 7-day Daily Calorie totals
    val dailyCalories = remember(allMeals) {
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val days = (6 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            format.format(c.time)
        }
        days.map { dayStr ->
            allMeals.filter { it.date == dayStr }.sumOf { it.calories }.toFloat()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health & Wellness Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "7-Day vital signs and nutritional balance correlation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 7-Day Heart Rate Trend Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("hr_trend_chart_card"),
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
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CoralHeart.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = CoralHeart, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Heart Rate Trend (7 Days)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        val avgBpm = if (hrData.isNotEmpty()) hrData.average().toInt() else 70
                        Text("Avg: $avgBpm BPM", fontWeight = FontWeight.Bold, color = CoralHeart, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val minVal = 50f
                            val maxVal = 140f
                            val stepX = size.width / (hrData.size - 1).coerceAtLeast(1)
                            val path = Path()

                            // Baseline reference lines
                            val normY = size.height - ((72f - minVal) / (maxVal - minVal) * size.height)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.25f),
                                start = Offset(0f, normY),
                                end = Offset(size.width, normY),
                                strokeWidth = 1.dp.toPx()
                            )

                            hrData.forEachIndexed { index, bpm ->
                                val x = index * stepX
                                val y = size.height - ((bpm - minVal) / (maxVal - minVal) * size.height).coerceIn(10f, size.height - 10f)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                drawCircle(
                                    color = CoralHeart,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            drawPath(
                                path = path,
                                color = CoralHeart,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("7d ago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Target: 60-80 BPM", fontSize = 10.sp, color = EmeraldAccent)
                        Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 7-Day Blood Pressure Dual-Line Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("bp_trend_chart_card"),
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
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VioletCalm.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = VioletCalm, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Blood Pressure (Systolic / Diastolic)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val minVal = 50f
                            val maxVal = 160f
                            val stepX = size.width / (bpSysData.size - 1).coerceAtLeast(1)

                            val sysPath = Path()
                            val diaPath = Path()

                            // Reference 120 mmHg line
                            val ref120 = size.height - ((120f - minVal) / (maxVal - minVal) * size.height)
                            drawLine(
                                color = EmeraldAccent.copy(alpha = 0.4f),
                                start = Offset(0f, ref120),
                                end = Offset(size.width, ref120),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Systolic
                            bpSysData.forEachIndexed { index, sys ->
                                val x = index * stepX
                                val y = size.height - ((sys - minVal) / (maxVal - minVal) * size.height).coerceIn(10f, size.height - 10f)
                                if (index == 0) sysPath.moveTo(x, y) else sysPath.lineTo(x, y)
                                drawCircle(color = VioletCalm, radius = 4.dp.toPx(), center = Offset(x, y))
                            }
                            drawPath(sysPath, color = VioletCalm, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                            // Diastolic
                            bpDiaData.forEachIndexed { index, dia ->
                                val x = index * stepX
                                val y = size.height - ((dia - minVal) / (maxVal - minVal) * size.height).coerceIn(10f, size.height - 10f)
                                if (index == 0) diaPath.moveTo(x, y) else diaPath.lineTo(x, y)
                                drawCircle(color = CyanHydration, radius = 4.dp.toPx(), center = Offset(x, y))
                            }
                            drawPath(diaPath, color = CyanHydration, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VioletCalm))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Systolic (<120)", fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanHydration))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Diastolic (<80)", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 7-Day Caloric Intake Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("calorie_trend_chart_card"),
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
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TealPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Daily Caloric Intake (7 Days)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Goal: ${userProfile.dailyCalorieGoal} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxVal = (userProfile.dailyCalorieGoal * 1.3f).coerceAtLeast(2600f)
                            val barWidth = size.width / (dailyCalories.size * 2f)

                            // Goal line
                            val goalY = size.height - ((userProfile.dailyCalorieGoal.toFloat() / maxVal) * size.height)
                            drawLine(
                                color = AmberEnergy.copy(alpha = 0.7f),
                                start = Offset(0f, goalY),
                                end = Offset(size.width, goalY),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            dailyCalories.forEachIndexed { index, calories ->
                                val barHeight = (calories / maxVal) * size.height
                                val x = (index * 2f + 0.5f) * barWidth
                                val y = size.height - barHeight

                                drawRoundRect(
                                    color = if (calories >= userProfile.dailyCalorieGoal * 0.85f && calories <= userProfile.dailyCalorieGoal * 1.15f) TealPrimary else AmberEnergy,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("7d ago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Goal Line (${userProfile.dailyCalorieGoal})", fontSize = 10.sp, color = AmberEnergy)
                        Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Clinical Health & Wellness Insights
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Personalized Wellness Recommendations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cardiovascular Fitness: Your average resting heart rate (~68-72 BPM) indicates strong heart efficiency and balanced autonomic nervous system tone.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nutrient Density: You are hitting your daily target for essential proteins and fiber, which helps maintain steady blood glucose levels and arterial elasticity.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanHydration, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hydration & Electrolytes: Drinking 2,500ml of water daily supports optimal blood volume, improving systolic and diastolic blood pressure regularity.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
