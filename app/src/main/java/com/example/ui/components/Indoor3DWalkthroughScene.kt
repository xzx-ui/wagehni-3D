package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.CampusBuilding
import com.example.model.CampusHall
import com.example.model.StepStage
import com.example.sensors.CompassTelemetry
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalDeepNavy
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import kotlin.math.roundToInt

@Composable
fun Indoor3DWalkthroughScene(
    activeHall: CampusHall,
    buildings: List<CampusBuilding>,
    currentStepIndex: Int,
    stepProgressFraction: Float,
    isContinuousWalkActive: Boolean,
    corridorYawOffset: Float,
    isArCameraMode: Boolean,
    isVoiceMuted: Boolean,
    compass: CompassTelemetry,
    isBookedByUser: Boolean,
    onSelectStep: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onToggleContinuousWalk: () -> Unit,
    onUpdateCorridorYaw: (Float) -> Unit,
    onResetCorridorYaw: () -> Unit,
    onToggleArCameraMode: () -> Unit,
    onToggleVoiceMute: () -> Unit,
    onSpeakCurrentStep: () -> Unit,
    onBookHall: (String) -> Unit,
    onOpenRadarPopup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentStep = activeHall.steps.getOrElse(currentStepIndex) { activeHall.steps.first() }
    val buildingColor = buildings.find { it.id == activeHall.buildingId }?.tacticalColor ?: NeonCyan

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted && !isArCameraMode) {
            onToggleArCameraMode()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "walkthrough_3d")
    val chevronFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chevron_flow"
    )
    val laserGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_glow"
    )

    val doorLabelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
    }
    val exitSignPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NeonEmerald.toArgb()
            textSize = 20f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalObsidian)
    ) {
        // Optional Live AR Camera Preview Background when AR mode is active & permission granted
        if (isArCameraMode && hasCameraPermission) {
            LiveCameraArPreview(modifier = Modifier.fillMaxSize())
        }

        // First-Person 3D Corridor Engine Canvas (or AR HUD Overlay when Camera is active)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        onUpdateCorridorYaw(dragAmount * 0.25f)
                    }
                }
                .testTag("indoor_3d_walkthrough_canvas")
        ) {
            val w = size.width
            val h = size.height

            // Dynamic Vanishing Point shifts horizontally with 360° user panning + turn direction
            val vpX = (w * 0.5f + corridorYawOffset * (w * 0.0045f) + currentStep.targetTurnAngle * 1.4f)
                .coerceIn(w * 0.18f, w * 0.82f)
            val vpY = h * 0.38f

            val backHalfW = w * 0.11f
            val backHalfH = h * 0.085f
            val backLeft = vpX - backHalfW
            val backRight = vpX + backHalfW
            val backTop = vpY - backHalfH
            val backBottom = vpY + backHalfH

            if (!isArCameraMode || !hasCameraPermission) {
                // 1. Ceiling Trapezoid
                val ceilingPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(backRight, backTop)
                    lineTo(backLeft, backTop)
                    close()
                }
                drawPath(
                    path = ceilingPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF060C18), Color(0xFF0F1E36)),
                        startY = 0f,
                        endY = backTop
                    )
                )

                // 2. Floor Trapezoid
                val floorPath = Path().apply {
                    moveTo(backLeft, backBottom)
                    lineTo(backRight, backBottom)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = floorPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B182B), Color(0xFF050A14)),
                        startY = backBottom,
                        endY = h
                    )
                )

                // 3. Left Wall Trapezoid
                val leftWallPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(backLeft, backTop)
                    lineTo(backLeft, backBottom)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = leftWallPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF102038), Color(0xFF0A1424)),
                        startX = 0f,
                        endX = backLeft
                    )
                )

                // 4. Right Wall Trapezoid
                val rightWallPath = Path().apply {
                    moveTo(w, 0f)
                    lineTo(backRight, backTop)
                    lineTo(backRight, backBottom)
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    path = rightWallPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0A1424), Color(0xFF102038)),
                        startX = backRight,
                        endX = w
                    )
                )

                // 5. Back End Portal / Elevator or Destination Wall
                drawRect(
                    color = Color(0xFF081220),
                    topLeft = Offset(backLeft, backTop),
                    size = Size(backHalfW * 2f, backHalfH * 2f)
                )
                drawRect(
                    color = buildingColor.copy(alpha = 0.7f),
                    topLeft = Offset(backLeft, backTop),
                    size = Size(backHalfW * 2f, backHalfH * 2f),
                    style = Stroke(width = 2.5f)
                )

                // Architectural Framing Lines from Corners to Vanishing Portal
                drawLine(NeonCyan.copy(alpha = 0.45f), Offset(0f, 0f), Offset(backLeft, backTop), 2f)
                drawLine(NeonCyan.copy(alpha = 0.45f), Offset(w, 0f), Offset(backRight, backTop), 2f)
                drawLine(NeonCyan.copy(alpha = 0.55f), Offset(0f, h), Offset(backLeft, backBottom), 2.5f)
                drawLine(NeonCyan.copy(alpha = 0.55f), Offset(w, h), Offset(backRight, backBottom), 2.5f)

                // 6. Scrolling Perspective Depth Slices (Ceiling LED Lights + Side Doors + Floor Grid)
                val depthSlices = 6
                val motionOffset = (stepProgressFraction * 4f + if (isContinuousWalkActive) chevronFlow else 0f) % 1f

                for (i in 0 until depthSlices) {
                    val rawT = ((i + motionOffset) / depthSlices.toFloat()).coerceIn(0.05f, 0.98f)
                    // Quadratic perspective curve so closer elements are larger and move faster
                    val t = rawT * rawT
                    val sliceLeft = backLeft * (1f - t)
                    val sliceRight = backRight + (w - backRight) * t
                    val sliceTop = backTop * (1f - t)
                    val sliceBottom = backBottom + (h - backBottom) * t

                    // Floor Tile Line
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.16f + 0.18f * t),
                        start = Offset(sliceLeft, sliceBottom),
                        end = Offset(sliceRight, sliceBottom),
                        strokeWidth = 1.5f
                    )

                    // Overhead Ceiling LED Light Strip
                    val ledLeft = sliceLeft + (sliceRight - sliceLeft) * 0.32f
                    val ledRight = sliceLeft + (sliceRight - sliceLeft) * 0.68f
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.45f + 0.45f * t),
                        start = Offset(ledLeft, sliceTop + 4f),
                        end = Offset(ledRight, sliceTop + 4f),
                        strokeWidth = 2f + 4f * t,
                        cap = StrokeCap.Round
                    )

                    // Side Architectural Doors with Real Room Numbers along Left & Right Walls
                    val doors = currentStep.sideDoorsInSegment
                    if (doors.isNotEmpty() && i % 2 == 1) {
                        val doorLabel = doors[i % doors.size]
                        val isTargetDoor = doorLabel == activeHall.code
                        val doorColor = if (isTargetDoor) NeonEmerald else buildingColor

                        val nextT = ((rawT + 0.11f).coerceAtMost(0.99f)).let { it * it }
                        val dLeftNext = backLeft * (1f - nextT)
                        val dRightNext = backRight + (w - backRight) * nextT
                        val dBottomNext = backBottom + (h - backBottom) * nextT
                        val dTopDoor = sliceTop + (sliceBottom - sliceTop) * 0.25f
                        val dTopDoorNext = backTop * (1f - nextT) + (dBottomNext - backTop * (1f - nextT)) * 0.25f

                        // Draw Right Wall Door Frame
                        val rightDoorPath = Path().apply {
                            moveTo(sliceRight, dTopDoor)
                            lineTo(dRightNext, dTopDoorNext)
                            lineTo(dRightNext, dBottomNext)
                            lineTo(sliceRight, sliceBottom)
                            close()
                        }
                        drawPath(
                            path = rightDoorPath,
                            color = doorColor.copy(alpha = if (isTargetDoor) 0.42f else 0.18f)
                        )
                        drawPath(
                            path = rightDoorPath,
                            color = doorColor.copy(alpha = 0.85f),
                            style = Stroke(width = if (isTargetDoor) 3.5f else 1.8f)
                        )

                        // Draw Left Wall Door Frame
                        val leftDoorPath = Path().apply {
                            moveTo(sliceLeft, dTopDoor)
                            lineTo(dLeftNext, dTopDoorNext)
                            lineTo(dLeftNext, dBottomNext)
                            lineTo(sliceLeft, sliceBottom)
                            close()
                        }
                        drawPath(
                            path = leftDoorPath,
                            color = buildingColor.copy(alpha = 0.14f)
                        )
                        drawPath(
                            path = leftDoorPath,
                            color = buildingColor.copy(alpha = 0.65f),
                            style = Stroke(width = 1.5f)
                        )

                        // Door Number Plaque
                        val plaqueX = (sliceRight + dRightNext) * 0.5f - 28f * t
                        val plaqueY = (dTopDoor + dTopDoorNext) * 0.5f + 22f * t
                        drawContext.canvas.nativeCanvas.apply {
                            doorLabelPaint.color = if (isTargetDoor) NeonEmerald.toArgb() else android.graphics.Color.WHITE
                            doorLabelPaint.textSize = (18f + 18f * t)
                            drawText(doorLabel, plaqueX, plaqueY, doorLabelPaint)
                        }
                    }
                }

                // Emergency Exit Sign & Elevator Indicator at Portal Top
                drawRect(
                    color = NeonEmerald.copy(alpha = 0.24f),
                    topLeft = Offset(vpX - 65f, backTop - 32f),
                    size = Size(130f, 26f)
                )
                drawRect(
                    color = NeonEmerald,
                    topLeft = Offset(vpX - 65f, backTop - 32f),
                    size = Size(130f, 26f),
                    style = Stroke(width = 1.5f)
                )
                drawContext.canvas.nativeCanvas.apply {
                    val portalLabel = when (currentStep.stage) {
                        StepStage.EXTERIOR -> "GATE • مدخل المبنى"
                        StepStage.VERTICAL -> "ELEVATOR • المصعد"
                        StepStage.CORRIDOR -> "EXIT • ممر القاعات"
                        StepStage.ARRIVAL -> "TARGET • ${activeHall.code}"
                    }
                    drawText(portalLabel, vpX, backTop - 12f, exitSignPaint)
                }
            }

            // 7. Neon Laser Guidance Lines on Floor (أشعة التوجيه النيونية الأرضية)
            val floorStartLeft = w * 0.36f
            val floorStartRight = w * 0.64f
            val floorStartCenter = w * 0.5f
            val targetFloorEnd = vpX + currentStep.targetTurnAngle * 0.9f

            // Outer glowing laser ribbon
            val laserRibbon = Path().apply {
                moveTo(floorStartLeft, h)
                lineTo(floorStartRight, h)
                lineTo(targetFloorEnd + 16f, backBottom)
                lineTo(targetFloorEnd - 16f, backBottom)
                close()
            }
            drawPath(
                path = laserRibbon,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.08f),
                        NeonCyan.copy(alpha = 0.32f * laserGlow)
                    ),
                    startY = backBottom,
                    endY = h
                )
            )

            // Left, Center & Right Neon Laser Beams
            drawLine(
                color = NeonCyan.copy(alpha = 0.95f * laserGlow),
                start = Offset(floorStartLeft, h),
                end = Offset(targetFloorEnd - 16f, backBottom),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = NeonCyan.copy(alpha = 0.95f * laserGlow),
                start = Offset(floorStartRight, h),
                end = Offset(targetFloorEnd + 16f, backBottom),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = NeonEmerald.copy(alpha = 0.9f),
                start = Offset(floorStartCenter, h),
                end = Offset(targetFloorEnd, backBottom),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // 8. Dynamic Flowing 3D Chevrons (الأسهم الانسيابية المتدفقة)
            val chevronCount = 5
            for (c in 0 until chevronCount) {
                // Chevrons flow forward from bottom (1.0) toward vanishing point (0.0)
                val progress = 1f - ((c / chevronCount.toFloat() + chevronFlow) % 1f)
                val perspective = progress * progress
                val cy = backBottom + (h - backBottom) * perspective
                val cx = targetFloorEnd + (floorStartCenter - targetFloorEnd) * perspective
                val halfSpan = 16f + 78f * perspective
                val tipRise = 10f + 28f * perspective

                val chevronColor = if (currentStep.stage == StepStage.ARRIVAL) NeonEmerald else NeonCyan
                val chevronAlpha = (0.35f + 0.65f * (1f - progress)).coerceIn(0.2f, 1f)

                val chevronPath = Path().apply {
                    moveTo(cx - halfSpan, cy + tipRise)
                    lineTo(cx, cy)
                    lineTo(cx + halfSpan, cy + tipRise)
                }
                drawPath(
                    path = chevronPath,
                    color = chevronColor.copy(alpha = chevronAlpha),
                    style = Stroke(width = 3f + 5f * perspective, cap = StrokeCap.Round)
                )
            }

            // 9. Floating AR / Holographic Room Plaque when near Arrival or in AR Mode
            if (isArCameraMode || currentStep.stage == StepStage.ARRIVAL) {
                val badgeW = 290f
                val badgeH = 95f
                val badgeLeft = (vpX - badgeW * 0.5f).coerceIn(16f, w - badgeW - 16f)
                val badgeTop = (vpY - 125f).coerceAtLeast(120f)

                drawRoundRect(
                    color = Color(0xE60B1528),
                    topLeft = Offset(badgeLeft, badgeTop),
                    size = Size(badgeW, badgeH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    color = NeonEmerald,
                    topLeft = Offset(badgeLeft, badgeTop),
                    size = Size(badgeW, badgeH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                    style = Stroke(width = 2.5f)
                )
                drawContext.canvas.nativeCanvas.apply {
                    doorLabelPaint.color = NeonEmerald.toArgb()
                    doorLabelPaint.textSize = 24f
                    drawText(
                        "${activeHall.code} • ${activeHall.officialName}",
                        badgeLeft + badgeW * 0.5f,
                        badgeTop + 36f,
                        doorLabelPaint
                    )
                    doorLabelPaint.color = TacticalGold.toArgb()
                    doorLabelPaint.textSize = 19f
                    drawText(
                        activeHall.currentLecturer,
                        badgeLeft + badgeW * 0.5f,
                        badgeTop + 68f,
                        doorLabelPaint
                    )
                }
            }
        }

        // Top Tactical Header (Mini-Radar + AR Toggle + Voice Guidance Controls + BLE/UWB Badge)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniRadarWidget(
                    compass = compass,
                    buildings = buildings,
                    activeHall = activeHall,
                    onClick = onOpenRadarPopup
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live AR Camera Toggle (Section 7.2)
                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                onToggleArCameraMode()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isArCameraMode && hasCameraPermission) NeonEmerald else TacticalDeepNavy.copy(alpha = 0.9f),
                            contentColor = if (isArCameraMode && hasCameraPermission) TacticalObsidian else NeonCyan
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .testTag("toggle_ar_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isArCameraMode && hasCameraPermission) Icons.Default.CameraAlt else Icons.Default.ViewInAr,
                            contentDescription = "الواقع المعزز بالكاميرا",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArCameraMode && hasCameraPermission) "وضع AR نشط" else "كاميرا AR",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Voice Mute / Unmute Button (Section 7.4)
                    IconButton(
                        onClick = onToggleVoiceMute,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TacticalDeepNavy.copy(alpha = 0.9f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.45f), CircleShape)
                            .testTag("toggle_voice_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "كتم أو تشغيل الصوت التكتيكي",
                            tint = if (isVoiceMuted) TacticalGold else NeonEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // BLE Beacon & UWB Micro-Location Live Telemetry Strip (Section 7.1)
            val remainingMeters = ((1f - stepProgressFraction) * currentStep.distanceMeters).roundToInt()
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = TacticalDeepNavy.copy(alpha = 0.88f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "منارة: ${activeHall.bleBeaconId} • دقة UWB ±${activeHall.uwbPrecisionMeters}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                    Text(
                        text = if (corridorYawOffset != 0f) {
                            "زاوية الالتفات: ${corridorYawOffset.roundToInt()}° (اضغط للمركز)"
                        } else {
                            "اسحب يميناً أو يساراً للالتفات 360°"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalGold,
                        modifier = Modifier.clickable { onResetCorridorYaw() }
                    )
                }
            }
        }

        // Bottom Walkthrough Control Deck (4-Step Timeline + Continuous Walk Simulator + Arrival Actions)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = TacticalDeepNavy.copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .border(
                    width = 1.5.dp,
                    color = if (currentStep.stage == StepStage.ARRIVAL) NeonEmerald else NeonCyan.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(22.dp)
                )
                .testTag("walkthrough_bottom_deck")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Step Selector Pills (Steps 1..4)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activeHall.steps.forEachIndexed { idx, step ->
                        val isCurrent = idx == currentStepIndex
                        val isCompleted = idx < currentStepIndex
                        val pillColor = when {
                            isCurrent && step.stage == StepStage.ARRIVAL -> NeonEmerald
                            isCurrent -> NeonCyan
                            isCompleted -> NeonEmerald.copy(alpha = 0.55f)
                            else -> TacticalCardSurface
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) pillColor.copy(alpha = 0.22f) else TacticalCardSurface,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, pillColor, RoundedCornerShape(10.dp))
                                .clickable { onSelectStep(idx) }
                                .testTag("walkthrough_step_pill_${idx + 1}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "خطوة ${step.stepIndex}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isCurrent) pillColor else TacticalTextSecondary
                                )
                                Text(
                                    text = step.stage.arabicLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TacticalTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Step Instruction & Distance Counter
                val remainingMeters = ((1f - stepProgressFraction) * currentStep.distanceMeters).roundToInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentStep.stage == StepStage.ARRIVAL) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "${activeHall.code} • ${currentStep.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStep.stage == StepStage.ARRIVAL) NeonEmerald else NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentStep.instruction,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TacticalTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Distance / Arrival Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TacticalCardSurface,
                        modifier = Modifier.border(
                            1.dp,
                            if (currentStep.stage == StepStage.ARRIVAL) NeonEmerald else TacticalGold,
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (currentStep.stage == StepStage.ARRIVAL) "تم الوصول" else "${remainingMeters}م",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStep.stage == StepStage.ARRIVAL) NeonEmerald else TacticalGold
                            )
                            Text(
                                text = activeHall.floorLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TacticalTextSecondary
                            )
                        }
                    }
                }

                if (isContinuousWalkActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { stepProgressFraction },
                        color = NeonCyan,
                        trackColor = TacticalCardSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Action Row: Previous Step | Continuous Walk Simulation | Speak Voice | Next Step
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onPreviousStep,
                        enabled = currentStepIndex > 0,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalCardSurface)
                            .testTag("prev_step_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الخطوة السابقة",
                            tint = if (currentStepIndex > 0) TacticalTextPrimary else TacticalTextSecondary
                        )
                    }

                    Button(
                        onClick = onToggleContinuousWalk,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isContinuousWalkActive) TacticalGold else NeonCyan,
                            contentColor = TacticalObsidian
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("continuous_walk_button")
                    ) {
                        Icon(
                            imageVector = if (isContinuousWalkActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isContinuousWalkActive) "إيقاف محاكي المشي" else "محاكي المشي المستمر",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onSpeakCurrentStep,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalCardSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .testTag("speak_voice_guidance_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "نطق التوجيه الصوتي العربي",
                            tint = NeonCyan
                        )
                    }

                    IconButton(
                        onClick = onNextStep,
                        enabled = currentStepIndex < activeHall.steps.lastIndex,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalCardSurface)
                            .testTag("next_step_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "الخطوة التالية",
                            tint = if (currentStepIndex < activeHall.steps.lastIndex) TacticalTextPrimary else TacticalTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Location Sharing («أنا هنا») & Hall Booking Row (Section 7.5)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = "📍 أنا هنا عبر تطبيق «وجهني» التكتيكي!\n" +
                                "القاعة: ${activeHall.code} - ${activeHall.officialName}\n" +
                                "المبنى: ${activeHall.buildingName} (${activeHall.floorLabel})\n" +
                                "منارة التوجيه الداخلي: ${activeHall.bleBeaconId}"
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "مشاركة موقع القاعة «أنا هنا»")
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_i_am_here_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShareLocation,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أنا هنا • مشاركة الموقع",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan
                        )
                    }

                    if (activeHall.isAvailableNow || isBookedByUser) {
                        Button(
                            onClick = { onBookHall(activeHall.code) },
                            enabled = !isBookedByUser,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonEmerald,
                                contentColor = TacticalObsidian
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("book_hall_walkthrough_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBookedByUser) "تم حجز القاعة" else "حجز القاعة الآن",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCameraArPreview(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                } catch (_: Exception) {
                    // Graceful fallback if virtual camera is unavailable
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}
