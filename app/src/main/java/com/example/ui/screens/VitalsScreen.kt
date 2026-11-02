package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VitalSign
import com.example.ui.BreathingPacerState
import com.example.ui.HeartRateScanState
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralHeart
import com.example.ui.theme.CyanHydration
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletCalm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VitalsScreen(
    vitals: List<VitalSign>,
    heartRateScan: HeartRateScanState,
    breathingPacer: BreathingPacerState,
    onStartHeartRateScan: () -> Unit,
    onStopHeartRateScan: () -> Unit,
    onFingerprintTouchStart: () -> Unit,
    onFingerprintTouchEnd: () -> Unit,
    onToggleBreathingPacer: () -> Unit,
    onLogVital: (type: String, primaryValue: Double, secondaryValue: Double?, unit: String, status: String, note: String) -> Unit,
    onDeleteVital: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeVitalsTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Vital Pulse", "Blood Pressure", "SpO2 & Lungs", "Temperature", "History")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("vitals_screen")
    ) {
        // Top Tab Selector
        TabRow(
            selectedTabIndex = activeVitalsTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeVitalsTab == index,
                    onClick = { activeVitalsTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (activeVitalsTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("vital_tab_$index")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeVitalsTab) {
                0 -> {
                    // FINGERPRINT OPTICAL VITAL PULSE CHECKER
                    item {
                        FingerprintPulseCard(
                            scanState = heartRateScan,
                            onStartScan = onStartHeartRateScan,
                            onStopScan = onStopHeartRateScan,
                            onTouchStart = onFingerprintTouchStart,
                            onTouchEnd = onFingerprintTouchEnd
                        )
                    }
                    item {
                        HeartHealthTipsCard()
                    }
                }
                1 -> {
                    // BLOOD PRESSURE CHECKER, NORMAL/NOT NORMAL INDICATOR & TREATMENT TIPS
                    item {
                        BloodPressureCard(
                            onLogBp = { sys, dia, status, note ->
                                onLogVital("BLOOD_PRESSURE", sys, dia, "mmHg", status, note)
                            },
                            onStartBreathingExercise = {
                                activeVitalsTab = 2 // Switch to SpO2 & Breathing tab
                                if (!breathingPacer.isActive) onToggleBreathingPacer()
                            }
                        )
                    }
                }
                2 -> {
                    // SPO2 & BREATHING PACER
                    item {
                        SpO2CheckerCard(onLogSpO2 = { spo2, status, note ->
                            onLogVital("SPO2", spo2, null, "%", status, note)
                        })
                    }
                    item {
                        BreathingPacerCard(
                            pacerState = breathingPacer,
                            onTogglePacer = onToggleBreathingPacer,
                            onLogBreaths = { breaths ->
                                onLogVital("RESPIRATORY_RATE", breaths, null, "brpm", "Normal", "Breathing session pace")
                            }
                        )
                    }
                }
                3 -> {
                    // BODY TEMPERATURE
                    item {
                        TemperatureCard(onLogTemp = { temp, status, note ->
                            onLogVital("TEMPERATURE", temp, null, "°F", status, note)
                        })
                    }
                }
                4 -> {
                    // VITALS LOG HISTORY
                    item {
                        Text(
                            text = "Vitals Reading History (${vitals.size} records)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(vitals, key = { it.id }) { vital ->
                        VitalHistoryItemCard(vital = vital, onDelete = { onDeleteVital(vital.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FingerprintPulseCard(
    scanState: HeartRateScanState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onTouchStart: () -> Unit,
    onTouchEnd: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FingerprintAnimation")

    // Laser scanning sweep line
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserSweep"
    )

    // Pulse expanding ripple
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (scanState.isScanning) 1.28f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (scanState.isScanning) 420 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RippleGlow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("heart_rate_checker_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CoralHeart.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            tint = CoralHeart,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Fingerprint Vital Pulse",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Touch & hold sensor for PPG pulse reading",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (scanState.isScanning) CoralHeart.copy(alpha = 0.15f) else EmeraldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (scanState.isScanning) "Scanning Pulse" else "Sensor Ready",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (scanState.isScanning) CoralHeart else EmeraldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Optical Fingerprint Scanner Target
            Box(
                modifier = Modifier
                    .size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding glowing ripple rings when finger is on sensor
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(rippleScale)
                        .clip(CircleShape)
                        .background(
                            if (scanState.isScanning) CoralHeart.copy(alpha = 0.14f)
                            else TealPrimary.copy(alpha = 0.08f)
                        )
                )

                // Biometric Outer Ring with Progress border
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(
                            if (scanState.isScanning) CoralHeart.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    CoralHeart,
                                    TealPrimary,
                                    CyanHydration,
                                    CoralHeart
                                )
                            ),
                            shape = CircleShape
                        )
                        .pointerInput(scanState.isScanning) {
                            detectTapGestures(
                                onPress = {
                                    onTouchStart()
                                    tryAwaitRelease()
                                    onTouchEnd()
                                }
                            )
                        }
                        .testTag("fingerprint_sensor_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    // Fingerprint Icon
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Fingerprint Scanner",
                        tint = if (scanState.isScanning) CoralHeart else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(76.dp)
                    )

                    // Laser Scanning Bar (Sweeping over fingerprint ridges during scan)
                    if (scanState.isScanning) {
                        Canvas(modifier = Modifier.size(80.dp)) {
                            val scanY = size.height * laserYRatio
                            drawLine(
                                color = CoralHeart,
                                start = Offset(0f, scanY),
                                end = Offset(size.width, scanY),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Soft glow around laser
                            drawCircle(
                                color = CoralHeart.copy(alpha = 0.35f),
                                radius = 8.dp.toPx(),
                                center = Offset(size.width / 2f, scanY)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Guidance prompt
            Surface(
                color = if (scanState.isScanning) CoralHeart.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (scanState.isScanning) "Hold finger steady on sensor..." else "Place and hold finger on sensor to read vital pulse",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (scanState.isScanning) CoralHeart else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live BPM & Waveform Display
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${scanState.currentBpm}",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BPM",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CoralHeart,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = scanState.status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Live Real-Time ECG Arterial Waveform Canvas
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val points = scanState.liveWaveform
                    if (points.isNotEmpty()) {
                        val path = Path()
                        val stepX = size.width / (points.size.coerceAtLeast(1) - 1).coerceAtLeast(1)
                        val midY = size.height / 2f
                        val scaleY = size.height * 0.4f

                        points.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = midY - (value * scaleY)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = CoralHeart,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    } else {
                        // Flat line before scan starts
                        drawLine(
                            color = CoralHeart.copy(alpha = 0.35f),
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            if (scanState.isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { scanState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = CoralHeart,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(scanState.progress * 100).toInt()}% completed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Results if complete
            scanState.resultBpm?.let { res ->
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = EmeraldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Resting Heart Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$res BPM", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldAccent)
                        }
                        scanState.hrvScore?.let { hrv ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HRV (Variability)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${hrv}ms", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Accessibility Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (scanState.isScanning) {
                    OutlinedButton(
                        onClick = onStopScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stop_heart_scan_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Measurement")
                    }
                } else {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_heart_scan_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralHeart)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Touch Sensor or Tap to Scan Pulse", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BloodPressureCard(
    onLogBp: (sys: Double, dia: Double, status: String, note: String) -> Unit,
    onStartBreathingExercise: () -> Unit
) {
    var systolic by remember { mutableDoubleStateOf(120.0) }
    var diastolic by remember { mutableDoubleStateOf(80.0) }
    var contextNote by remember { mutableStateOf("Resting Seated") }
    val notes = listOf("Resting Seated", "Morning Wakeup", "Post Exercise", "Before Bed")

    // AHA Classification
    val status = remember(systolic, diastolic) {
        when {
            systolic > 180 || diastolic > 120 -> "Crisis"
            systolic >= 140 || diastolic >= 90 -> "Stage 2"
            systolic in 130.0..139.9 || diastolic in 80.0..89.9 -> "Stage 1"
            systolic in 120.0..129.9 && diastolic < 80.0 -> "Elevated"
            else -> "Normal"
        }
    }

    val isNormal = remember(systolic, diastolic) {
        systolic < 120.0 && diastolic < 80.0
    }

    // Mean Arterial Pressure (MAP) = Diastolic + (1/3 * (Systolic - Diastolic))
    val map = remember(systolic, diastolic) {
        (diastolic + ((systolic - diastolic) / 3.0)).toInt()
    }
    val pulsePressure = remember(systolic, diastolic) {
        (systolic - diastolic).toInt()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("blood_pressure_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VioletCalm.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = VioletCalm)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Blood Pressure Module", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("AHA Clinical Standards & Normalcy Analyzer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PROMINENT NORMAL VS NOT NORMAL INDICATION BANNER
            if (isNormal) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bp_status_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Normal Blood Pressure",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BLOOD PRESSURE IS NORMAL",
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldAccent,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Systolic < 120 mmHg and Diastolic < 80 mmHg. Peripheral arterial resistance is healthy.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bp_status_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (status) {
                            "Elevated" -> AmberEnergy.copy(alpha = 0.14f)
                            "Stage 1" -> Color(0xFFF97316).copy(alpha = 0.14f)
                            else -> Color(0xFFF43F5E).copy(alpha = 0.14f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (status) {
                                        "Elevated" -> AmberEnergy
                                        "Stage 1" -> Color(0xFFF97316)
                                        else -> Color(0xFFF43F5E)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Not Normal",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BLOOD PRESSURE IS NOT NORMAL: ${status.uppercase()}",
                                fontWeight = FontWeight.ExtraBold,
                                color = when (status) {
                                    "Elevated" -> AmberEnergy
                                    "Stage 1" -> Color(0xFFF97316)
                                    else -> Color(0xFFF43F5E)
                                },
                                fontSize = 13.sp
                            )
                            Text(
                                text = when (status) {
                                    "Elevated" -> "Arterial pressure is elevated (120–129 mmHg). Lifestyle action prevents progression to hypertension."
                                    "Stage 1" -> "Stage 1 Hypertension (130–139/80–89 mmHg). Heart is experiencing increased vascular workload."
                                    "Stage 2" -> "Stage 2 Hypertension (≥140/≥90 mmHg). Active intervention and medical guidance recommended."
                                    else -> "🚨 Hypertensive Crisis (>180/>120 mmHg)! Seek emergency healthcare evaluation."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Reading Big Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${systolic.toInt()}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                Text(
                    text = "${diastolic.toInt()}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "mmHg",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Secondary calculated clinical metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("MAP: $map mmHg", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Pulse Pressure: $pulsePressure mmHg", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Systolic Slider
            Text(
                text = "Systolic Pressure (Upper number): ${systolic.toInt()} mmHg",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = systolic.toFloat(),
                onValueChange = { systolic = it.toDouble() },
                valueRange = 90f..200f,
                steps = 110,
                modifier = Modifier.testTag("systolic_slider"),
                colors = SliderDefaults.colors(thumbColor = VioletCalm, activeTrackColor = VioletCalm)
            )

            // Diastolic Slider
            Text(
                text = "Diastolic Pressure (Lower number): ${diastolic.toInt()} mmHg",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = diastolic.toFloat(),
                onValueChange = { diastolic = it.toDouble() },
                valueRange = 50f..130f,
                steps = 80,
                modifier = Modifier.testTag("diastolic_slider"),
                colors = SliderDefaults.colors(thumbColor = VioletCalm, activeTrackColor = VioletCalm)
            )

            // Context Note Selection
            Spacer(modifier = Modifier.height(8.dp))
            Text("Measurement Context:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note ->
                    FilterChip(
                        selected = contextNote == note,
                        onClick = { contextNote = note },
                        label = { Text(note, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onLogBp(systolic, diastolic, status, contextNote)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("log_blood_pressure_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletCalm)
            ) {
                Text("Save Blood Pressure Record", fontWeight = FontWeight.Bold)
            }

            // CLINICAL RECOMMENDATIONS: WHAT TO DO TO MAKE BLOOD PRESSURE NORMAL
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "What To Do To Make Blood Pressure Normal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 1. Immediate Stress Relief via Breathing
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🫁", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Stimulate Vagal Tone with 4-7-8 Breathing", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(
                                "Controlled rhythmic breathing acutely reduces sympathetic outflow, relaxing blood vessel walls.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onStartBreathingExercise,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Start 4-7-8 Breathing Now", fontSize = 11.sp)
                            }
                        }
                    }

                    // 2. DASH Diet & Potassium
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🥑", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Adopt DASH Diet & Potassium-Rich Foods", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(
                                "Increase dietary potassium (3,500–4,700 mg/day) from avocados, spinach, sweet potatoes, and bananas to promote sodium excretion by the kidneys.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Sodium Restriction
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🧂", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Restrict Sodium to Under 1,500 mg/day", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(
                                "Cutting excess table salt and avoiding packaged processed meats directly lowers blood volume and arterial strain.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 4. Hydration & Physical Activity
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🏃", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("30 Minutes Daily Aerobic Exercise & Hydration", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(
                                "Regular brisk walking lowers systolic pressure by 5–8 mmHg. Drink 2,500 ml water to avoid blood hyperviscosity.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 5. Physician Check
                    if (!isNormal) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text("🩺", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Clinical Guidance", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = CoralHeart)
                                Text(
                                    "If systolic remains ≥140 mmHg or diastolic ≥90 mmHg over consecutive days, consult your physician for formal assessment.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpO2CheckerCard(
    onLogSpO2: (spo2: Double, status: String, note: String) -> Unit
) {
    var spo2Value by remember { mutableDoubleStateOf(98.0) }
    val status = if (spo2Value >= 95.0) "Normal (Optimal)" else if (spo2Value >= 90.0) "Mild Hypoxemia" else "Urgent Warning"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("spo2_checker_card"),
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanHydration.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = CyanHydration)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Blood Oxygen Saturation (SpO2)", fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = if (spo2Value >= 95) EmeraldAccent.copy(alpha = 0.15f) else AmberEnergy.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (spo2Value >= 95) EmeraldAccent else AmberEnergy,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${spo2Value.toInt()}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "% SpO2",
                    fontSize = 14.sp,
                    color = CyanHydration,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Slider(
                value = spo2Value.toFloat(),
                onValueChange = { spo2Value = it.toDouble() },
                valueRange = 85f..100f,
                steps = 15,
                colors = SliderDefaults.colors(thumbColor = CyanHydration, activeTrackColor = CyanHydration),
                modifier = Modifier.testTag("spo2_slider")
            )

            Button(
                onClick = { onLogSpO2(spo2Value, if (spo2Value >= 95) "Normal" else "Warning", "Pulse oximeter log") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_spo2_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanHydration)
            ) {
                Text("Log Blood Oxygen", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun BreathingPacerCard(
    pacerState: BreathingPacerState,
    onTogglePacer: () -> Unit,
    onLogBreaths: (Double) -> Unit
) {
    val animatedPacerScale by animateFloatAsState(
        targetValue = when (pacerState.phase) {
            "Inhale" -> 1.35f
            "Hold" -> 1.35f
            "Exhale" -> 0.85f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 3500),
        label = "PacerBreathScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("breathing_pacer_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TealSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = TealSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("4-7-8 Breathing Pacer", fontWeight = FontWeight.Bold)
                        Text("Relaxes nervous system & calms pulse", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("Cycles: ${pacerState.cycleCount}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(if (pacerState.isActive) animatedPacerScale else 1f)
                    .clip(CircleShape)
                    .background(TealSecondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .clip(CircleShape)
                        .background(TealSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (pacerState.isActive) pacerState.phase else "Pacer",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        if (pacerState.isActive) {
                            Text(
                                text = "${pacerState.secondsRemaining}s",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTogglePacer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_breathing_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealSecondary)
                ) {
                    Icon(
                        imageVector = if (pacerState.isActive) Icons.Default.Stop else Icons.Default.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (pacerState.isActive) "Stop Pacer" else "Start Breathing")
                }

                OutlinedButton(
                    onClick = { onLogBreaths(15.0) },
                    modifier = Modifier.testTag("log_breaths_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log 15 brpm", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TemperatureCard(
    onLogTemp: (temp: Double, status: String, note: String) -> Unit
) {
    var tempF by remember { mutableDoubleStateOf(98.6) }
    val status = when {
        tempF >= 100.4 -> "High Fever"
        tempF >= 99.5 -> "Low Grade Fever"
        tempF < 97.0 -> "Hypothermia"
        else -> "Normal"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("temperature_card"),
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberEnergy.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = AmberEnergy)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Body Temperature", fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = if (status == "Normal") EmeraldAccent.copy(alpha = 0.15f) else AmberEnergy.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (status == "Normal") EmeraldAccent else AmberEnergy,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                val formatted = String.format(Locale.US, "%.1f", tempF)
                val cVal = String.format(Locale.US, "%.1f", (tempF - 32) * 5 / 9)
                Text(
                    text = formatted,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "°F ($cVal °C)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Slider(
                value = tempF.toFloat(),
                onValueChange = { tempF = it.toDouble() },
                valueRange = 96.0f..104.0f,
                steps = 80,
                colors = SliderDefaults.colors(thumbColor = AmberEnergy, activeTrackColor = AmberEnergy),
                modifier = Modifier.testTag("temp_slider")
            )

            Button(
                onClick = { onLogTemp(tempF, status, "Oral temperature reading") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_temp_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberEnergy)
            ) {
                Text("Log Temperature", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun VitalHistoryItemCard(
    vital: VitalSign,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
    val formattedTime = remember(vital.timestamp) { timeFormat.format(Date(vital.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("vital_history_item_${vital.id}"),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (vital.type) {
                                "HEART_RATE" -> CoralHeart.copy(alpha = 0.15f)
                                "BLOOD_PRESSURE" -> VioletCalm.copy(alpha = 0.15f)
                                "SPO2" -> CyanHydration.copy(alpha = 0.15f)
                                "RESPIRATORY_RATE" -> TealSecondary.copy(alpha = 0.15f)
                                else -> AmberEnergy.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (vital.type) {
                            "HEART_RATE" -> Icons.Default.Favorite
                            "BLOOD_PRESSURE" -> Icons.Default.Speed
                            "SPO2" -> Icons.Default.Opacity
                            "RESPIRATORY_RATE" -> Icons.Default.Air
                            else -> Icons.Default.Thermostat
                        },
                        contentDescription = null,
                        tint = when (vital.type) {
                            "HEART_RATE" -> CoralHeart
                            "BLOOD_PRESSURE" -> VioletCalm
                            "SPO2" -> CyanHydration
                            "RESPIRATORY_RATE" -> TealSecondary
                            else -> AmberEnergy
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val displayValue = if (vital.secondaryValue != null) {
                        "${vital.primaryValue.toInt()}/${vital.secondaryValue.toInt()} ${vital.unit}"
                    } else if (vital.type == "TEMPERATURE") {
                        String.format(Locale.US, "%.1f %s", vital.primaryValue, vital.unit)
                    } else {
                        "${vital.primaryValue.toInt()} ${vital.unit}"
                    }

                    Text(
                        text = displayValue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$formattedTime • ${vital.contextNote}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when (vital.status) {
                        "Normal", "Normal (Optimal)" -> EmeraldAccent.copy(alpha = 0.15f)
                        "Elevated" -> AmberEnergy.copy(alpha = 0.15f)
                        else -> Color(0xFFF43F5E).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = vital.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (vital.status) {
                            "Normal", "Normal (Optimal)" -> EmeraldAccent
                            "Elevated" -> AmberEnergy
                            else -> Color(0xFFF43F5E)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
fun HeartHealthTipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Fingerprint Optical Pulse Tracking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Holding your finger on the optical sensor measures light absorption changes across the capillary bed as arterial blood pulses. For optimal accuracy, rest quietly for 1–2 minutes before scanning.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
