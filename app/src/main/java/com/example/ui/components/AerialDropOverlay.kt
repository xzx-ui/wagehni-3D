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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CampusBuilding
import com.example.model.CampusHall
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalDeepNavy
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AerialDropOverlay(
    altitudeMeters: Int,
    targetHall: CampusHall,
    targetBuilding: CampusBuilding,
    onSkipToWalkthrough: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aerial_drop")
    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticle_rot"
    )
    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticle_pulse"
    )

    // Progress from 280m down to 12m (0.0 -> 1.0)
    val descentProgress = ((280f - altitudeMeters.coerceIn(12, 280)) / (280f - 12f)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalObsidian.copy(alpha = 0.94f))
            .testTag("aerial_drop_overlay")
    ) {
        // 1. Tactical Aerial Descent Canvas (Zooming Building Wireframe + Polygonal & Circular Targeting Grid)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.45f)
            val maxR = w.coerceAtMost(h) * 0.42f

            // Expanding ground perspective grid simulating high-speed vertical descent
            val zoomFactor = 0.35f + descentProgress * 1.85f
            val gridCount = 8
            for (i in -gridCount..gridCount) {
                val offset = i * (maxR * 0.25f) * zoomFactor
                drawLine(
                    color = NeonCyan.copy(alpha = 0.16f),
                    start = Offset(0f, center.y + offset),
                    end = Offset(w, center.y + offset),
                    strokeWidth = 1.2f
                )
                drawLine(
                    color = NeonCyan.copy(alpha = 0.16f),
                    start = Offset(center.x + offset, 0f),
                    end = Offset(center.x + offset, h),
                    strokeWidth = 1.2f
                )
            }

            // Target Building Roof Footprint scaling up as altitude drops from 280m -> 12m
            val bldHalfSize = maxR * (0.18f + descentProgress * 0.45f)
            drawRect(
                color = targetBuilding.tacticalColor.copy(alpha = 0.20f),
                topLeft = Offset(center.x - bldHalfSize, center.y - bldHalfSize * 0.75f),
                size = Size(bldHalfSize * 2f, bldHalfSize * 1.5f)
            )
            drawRect(
                color = targetBuilding.tacticalColor.copy(alpha = 0.85f),
                topLeft = Offset(center.x - bldHalfSize, center.y - bldHalfSize * 0.75f),
                size = Size(bldHalfSize * 2f, bldHalfSize * 1.5f),
                style = Stroke(width = 3f)
            )

            // Outer Circular HUD Ring
            drawCircle(
                color = NeonCyan.copy(alpha = 0.55f),
                radius = maxR * 0.85f * reticlePulse,
                center = center,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = NeonEmerald.copy(alpha = 0.75f),
                radius = maxR * 0.52f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Rotating Hexagonal / Polygonal Tactical Reticle
            rotate(degrees = reticleRotation, pivot = center) {
                val hexPath = Path()
                val hexRadius = maxR * 0.68f
                for (v in 0 until 6) {
                    val angleRad = Math.toRadians((v * 60.0))
                    val px = center.x + (cos(angleRad) * hexRadius).toFloat()
                    val py = center.y + (sin(angleRad) * hexRadius).toFloat()
                    if (v == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
                }
                hexPath.close()
                drawPath(
                    path = hexPath,
                    color = TacticalGold.copy(alpha = 0.7f),
                    style = Stroke(width = 2.2f)
                )
            }

            // Corner Lock Brackets around Target Point
            val bracketOffset = maxR * (0.38f - descentProgress * 0.16f)
            val bracketLen = 26f
            // Top-Left
            drawLine(NeonEmerald, Offset(center.x - bracketOffset, center.y - bracketOffset), Offset(center.x - bracketOffset + bracketLen, center.y - bracketOffset), 4f)
            drawLine(NeonEmerald, Offset(center.x - bracketOffset, center.y - bracketOffset), Offset(center.x - bracketOffset, center.y - bracketOffset + bracketLen), 4f)
            // Top-Right
            drawLine(NeonEmerald, Offset(center.x + bracketOffset, center.y - bracketOffset), Offset(center.x + bracketOffset - bracketLen, center.y - bracketOffset), 4f)
            drawLine(NeonEmerald, Offset(center.x + bracketOffset, center.y - bracketOffset), Offset(center.x + bracketOffset, center.y - bracketOffset + bracketLen), 4f)
            // Bottom-Left
            drawLine(NeonEmerald, Offset(center.x - bracketOffset, center.y + bracketOffset), Offset(center.x - bracketOffset + bracketLen, center.y + bracketOffset), 4f)
            drawLine(NeonEmerald, Offset(center.x - bracketOffset, center.y + bracketOffset), Offset(center.x - bracketOffset, center.y + bracketOffset - bracketLen), 4f)
            // Bottom-Right
            drawLine(NeonEmerald, Offset(center.x + bracketOffset, center.y + bracketOffset), Offset(center.x + bracketOffset - bracketLen, center.y + bracketOffset), 4f)
            drawLine(NeonEmerald, Offset(center.x + bracketOffset, center.y + bracketOffset), Offset(center.x + bracketOffset, center.y + bracketOffset - bracketLen), 4f)

            // Center Target Lock Point
            drawCircle(
                color = NeonEmerald,
                radius = 7f,
                center = center
            )

            // Left Vertical Altitude Tape
            val tapeX = 36f
            val tapeTop = h * 0.22f
            val tapeBottom = h * 0.68f
            drawLine(
                color = NeonCyan.copy(alpha = 0.5f),
                start = Offset(tapeX, tapeTop),
                end = Offset(tapeX, tapeBottom),
                strokeWidth = 3f
            )
            val markerY = tapeTop + (tapeBottom - tapeTop) * descentProgress
            drawCircle(
                color = NeonEmerald,
                radius = 9f,
                center = Offset(tapeX, markerY)
            )
        }

        // 2. Top Tactical Telemetry Header (ALT + Target Stability ±0.5m)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TacticalDeepNavy.copy(alpha = 0.92f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlightLand,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الهبوط السينمائي الجوي التكتيكي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TacticalTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Target Stability ±0.5m • ثبات الهدف الموجه",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonEmerald
                            )
                        }
                    }

                    // Digital Altitude Counter (ALT: 280m -> 12m)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TacticalCardSurface,
                        modifier = Modifier.border(1.dp, TacticalGold, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ALTITUDE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalGold
                            )
                            Text(
                                text = "ALT ${altitudeMeters}m",
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("aerial_altitude_readout")
                            )
                        }
                    }
                }
            }
        }

        // 3. Luxury Glassmorphic Bottom Target Card (بطاقة الهدف السفلية)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = TacticalDeepNavy.copy(alpha = 0.94f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .border(1.5.dp, NeonEmerald.copy(alpha = 0.75f), RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تم تثبيت إحداثيات الهدف • القاعة ${targetHall.code}",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonEmerald
                        )
                    }
                    Text(
                        text = targetHall.floorLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = TacticalGold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = targetHall.officialName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TacticalTextPrimary
                )
                Text(
                    text = "${targetHall.buildingName} • السعة: ${targetHall.capacityLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TacticalTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSkipToWalkthrough,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = TacticalObsidian
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("skip_aerial_drop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الدخول المباشر للملاحة الداخلية بالمنظور الأول (3D)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
