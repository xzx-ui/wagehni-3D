package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CampusBuilding
import com.example.model.CampusHall
import com.example.sensors.CompassTelemetry
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalDeepNavy
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MiniRadarWidget(
    compass: CompassTelemetry,
    buildings: List<CampusBuilding>,
    activeHall: CampusHall,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TacticalDeepNavy.copy(alpha = 0.88f))
            .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("mini_radar_widget")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp)
        ) {
            Canvas(modifier = Modifier.size(42.dp)) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    color = TacticalObsidian,
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.35f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.18f),
                    radius = radius * 0.55f,
                    center = center,
                    style = Stroke(width = 1f)
                )

                // Crosshairs
                drawLine(
                    color = NeonCyan.copy(alpha = 0.22f),
                    start = Offset(center.x, 2f),
                    end = Offset(center.x, size.height - 2f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = NeonCyan.copy(alpha = 0.22f),
                    start = Offset(2f, center.y),
                    end = Offset(size.width - 2f, center.y),
                    strokeWidth = 1f
                )

                // Rotating Radar Sweep
                rotate(degrees = sweepAngle, pivot = center) {
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonEmerald, Color.Transparent),
                            start = center,
                            end = Offset(center.x, 0f)
                        ),
                        start = center,
                        end = Offset(center.x, 2f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }

                // Building Blips rotated relative to user azimuth
                buildings.forEach { b ->
                    val relRad = Math.toRadians((b.radarAngleDeg - compass.azimuthDegrees - 90f).toDouble())
                    val dist = radius * 0.75f * b.radarDistanceRatio
                    val bx = center.x + (cos(relRad) * dist).toFloat()
                    val by = center.y + (sin(relRad) * dist).toFloat()
                    drawCircle(
                        color = b.tacticalColor,
                        radius = if (b.id == activeHall.buildingId) 4.2f else 2.8f,
                        center = Offset(bx, by)
                    )
                }

                // Active Hall Target Lock Blip
                val targetRad = Math.toRadians((activeHall.radarAzimuthDeg - compass.azimuthDegrees - 90f).toDouble())
                val tx = center.x + (cos(targetRad) * radius * 0.6f).toFloat()
                val ty = center.y + (sin(targetRad) * radius * 0.6f).toFloat()
                drawCircle(
                    color = NeonEmerald,
                    radius = 5f,
                    center = Offset(tx, ty),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${compass.azimuthDegrees.roundToInt()}° ${compass.cardinalCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
            }
            Text(
                text = "الرادار: ${compass.cardinalArabic}",
                style = MaterialTheme.typography.bodyMedium,
                color = TacticalTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TacticalRadarDialog(
    compass: CompassTelemetry,
    buildings: List<CampusBuilding>,
    halls: List<CampusHall>,
    activeHall: CampusHall,
    onSelectHallAndDrop: (CampusHall) -> Unit,
    onManualAzimuthChange: (Float) -> Unit,
    onResumeHardwareSensor: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "full_radar")
    val radarSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "full_sweep"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blip_pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = TacticalDeepNavy,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.5.dp, NeonCyan.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                .testTag("tactical_radar_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "الرادار التكتيكي",
                            tint = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "الرادار والبوصلة التكتيكية الحية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TacticalTextPrimary
                            )
                            Text(
                                text = if (compass.isHardwareSensorActive) {
                                    "مستشعر المغناطيسية والتسارع نشط • Azimuth Smoothing"
                                } else {
                                    "وضع المحاكاة التكتيكية للبوصلة (توجيه 0° - 360°)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonEmerald
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_radar_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق الرادار",
                            tint = TacticalTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry Readout Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalCardSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الزاوية الدقيقة", style = MaterialTheme.typography.bodyMedium, color = TacticalTextSecondary)
                        Text(
                            text = "${compass.azimuthDegrees.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الاتجاه الجغرافي", style = MaterialTheme.typography.bodyMedium, color = TacticalTextSecondary)
                        Text(
                            text = "${compass.cardinalArabic} (${compass.cardinalCode})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TacticalGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الهدف المقفل", style = MaterialTheme.typography.bodyMedium, color = TacticalTextSecondary)
                        Text(
                            text = activeHall.code,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Large Radar & Compass Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(250.dp)
                ) {
                    Canvas(modifier = Modifier.size(240.dp)) {
                        val radius = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Deep Radar Background
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0E223D), TacticalObsidian),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )

                        // Concentric Polar Rings
                        listOf(1f, 0.72f, 0.45f, 0.2f).forEach { ratio ->
                            drawCircle(
                                color = NeonCyan.copy(alpha = if (ratio == 1f) 0.65f else 0.25f),
                                radius = radius * ratio,
                                center = center,
                                style = Stroke(width = if (ratio == 1f) 2.5f else 1.2f)
                            )
                        }

                        // Diagonal & Orthogonal Grid Lines
                        for (angle in 0 until 360 step 45) {
                            val rad = Math.toRadians(angle.toDouble())
                            val endX = center.x + (cos(rad) * radius).toFloat()
                            val endY = center.y + (sin(rad) * radius).toFloat()
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.18f),
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 1f
                            )
                        }

                        // Rotating Radar Sweep Beam
                        rotate(degrees = radarSweep, pivot = center) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0f to Color.Transparent,
                                    0.82f to Color.Transparent,
                                    0.95f to NeonEmerald.copy(alpha = 0.22f),
                                    1f to NeonEmerald.copy(alpha = 0.55f),
                                    center = center
                                ),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = true
                            )
                            drawLine(
                                color = NeonEmerald,
                                start = center,
                                end = Offset(center.x + radius, center.y),
                                strokeWidth = 2.5f
                            )
                        }

                        // Compass Needle (North/South) rotated by -azimuthDegrees
                        rotate(degrees = -compass.azimuthDegrees, pivot = center) {
                            val northPath = Path().apply {
                                moveTo(center.x, center.y - radius * 0.86f)
                                lineTo(center.x - 8f, center.y)
                                lineTo(center.x + 8f, center.y)
                                close()
                            }
                            drawPath(northPath, color = NeonCyan.copy(alpha = 0.75f))

                            val southPath = Path().apply {
                                moveTo(center.x, center.y + radius * 0.45f)
                                lineTo(center.x - 6f, center.y)
                                lineTo(center.x + 6f, center.y)
                                close()
                            }
                            drawPath(southPath, color = TacticalGold.copy(alpha = 0.45f))
                        }

                        // Draw Hall & Building Blips
                        halls.forEach { hall ->
                            val bld = buildings.find { it.id == hall.buildingId }
                            val distRatio = bld?.radarDistanceRatio ?: 0.55f
                            val relRad = Math.toRadians((hall.radarAzimuthDeg - compass.azimuthDegrees - 90f).toDouble())
                            val bx = center.x + (cos(relRad) * radius * distRatio).toFloat()
                            val by = center.y + (sin(relRad) * radius * distRatio).toFloat()
                            val blipColor = bld?.tacticalColor ?: NeonCyan

                            if (hall.code == activeHall.code) {
                                drawCircle(
                                    color = NeonEmerald.copy(alpha = 0.35f),
                                    radius = pulseRadius,
                                    center = Offset(bx, by)
                                )
                                drawCircle(
                                    color = NeonEmerald,
                                    radius = 7f,
                                    center = Offset(bx, by)
                                )
                            } else {
                                drawCircle(
                                    color = blipColor,
                                    radius = 5f,
                                    center = Offset(bx, by)
                                )
                            }
                        }

                        // Center User Beacon
                        drawCircle(
                            color = TacticalGold,
                            radius = 5.5f,
                            center = center
                        )
                    }

                    // Cardinal Labels Overlay
                    Text(
                        text = "شمال N",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = "جنوب S",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalGold,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "شرق E",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "غرب W",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Compass Calibration / Azimuth Rotation Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "محاكاة تدوير زاوية البوصلة (0° - 360°)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TacticalTextSecondary
                        )
                        TextButton(onClick = onResumeHardwareSensor) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("المستشعر الفيزيائي", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Slider(
                        value = compass.azimuthDegrees,
                        onValueChange = onManualAzimuthChange,
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("radar_azimuth_slider")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Blip Targets (Halls on Radar)
                Text(
                    text = "أهداف القاعات المرصودة على الرادار (انقر للهبوط التكتيكي):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TacticalTextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    halls.take(4).chunked(2).forEach { rowHalls ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowHalls.forEach { hall ->
                                val isSelected = hall.code == activeHall.code
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onSelectHallAndDrop(hall)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) NeonCyan.copy(alpha = 0.22f) else TacticalCardSurface,
                                        contentColor = if (isSelected) NeonCyan else TacticalTextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NeonCyan else NeonCyan.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlightLand,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${hall.code} • ${hall.radarAzimuthDeg.roundToInt()}°",
                                        style = MaterialTheme.typography.labelLarge
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
