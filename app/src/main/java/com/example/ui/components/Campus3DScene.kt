package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private data class ProjectedBuildingHitbox(
    val building: CampusBuilding,
    val screenCenter: Offset,
    val hitRadius: Float
)

@Composable
fun Campus3DSceneScreen(
    buildings: List<CampusBuilding>,
    halls: List<CampusHall>,
    selectedBuilding: CampusBuilding,
    selectedFloorFilter: Int?,
    activeHall: CampusHall,
    compass: CompassTelemetry,
    showIotLiveOverlay: Boolean,
    onlyWheelchairAccessible: Boolean,
    scheduleAlertVisible: Boolean,
    onSelectBuilding: (CampusBuilding) -> Unit,
    onSelectFloorFilter: (Int?) -> Unit,
    onToggleIotOverlay: () -> Unit,
    onToggleWheelchair: () -> Unit,
    onDismissScheduleAlert: () -> Unit,
    onOpenRadarPopup: () -> Unit,
    onLaunchAerialDrop: (CampusHall) -> Unit,
    modifier: Modifier = Modifier
) {
    var cameraYawDeg by remember { mutableFloatStateOf(38f) }
    var cameraPitchDeg by remember { mutableFloatStateOf(52f) }
    var cameraZoom by remember { mutableFloatStateOf(1.05f) }
    var autoOrbit by remember { mutableStateOf(false) }

    val hitboxes = remember { mutableListOf<ProjectedBuildingHitbox>() }

    LaunchedEffect(autoOrbit) {
        while (autoOrbit) {
            delay(32L)
            cameraYawDeg = (cameraYawDeg + 0.55f) % 360f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "campus_3d_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val laserFlowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser_phase"
    )

    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val subLabelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NeonCyan.toArgb()
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(TacticalObsidian)) {
        // 1. Native 3D Isometric / Perspective Canvas Engine
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        cameraYawDeg = (cameraYawDeg - pan.x * 0.35f + 360f) % 360f
                        cameraPitchDeg = (cameraPitchDeg + pan.y * 0.22f).coerceIn(28f, 75f)
                        cameraZoom = (cameraZoom * zoom).coerceIn(0.68f, 1.85f)
                    }
                }
                .pointerInput(buildings, cameraYawDeg, cameraPitchDeg, cameraZoom) {
                    detectTapGestures { tapOffset ->
                        val tapped = hitboxes
                            .minByOrNull { hypot(tapOffset.x - it.screenCenter.x, tapOffset.y - it.screenCenter.y) }
                            ?.takeIf {
                                hypot(tapOffset.x - it.screenCenter.x, tapOffset.y - it.screenCenter.y) <= it.hitRadius
                            }
                        if (tapped != null) {
                            onSelectBuilding(tapped.building)
                        }
                    }
                }
                .testTag("campus_3d_canvas")
        ) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.45f
            val baseScale = w.coerceAtMost(h) * 0.42f * cameraZoom

            val yawRad = Math.toRadians(cameraYawDeg.toDouble())
            val pitchRad = Math.toRadians(cameraPitchDeg.toDouble())
            val cosYaw = cos(yawRad).toFloat()
            val sinYaw = sin(yawRad).toFloat()
            val sinPitch = sin(pitchRad).toFloat()
            val cosPitch = cos(pitchRad).toFloat()

            fun project3D(gx: Float, gy: Float, gz: Float): Offset {
                val rx = gx * cosYaw - gz * sinYaw
                val rz = gx * sinYaw + gz * cosYaw
                // Perspective foreshortening
                val perspective = 1f / (1f + rz * 0.22f)
                val sx = cx + rx * baseScale * perspective
                val sy = cy + (rz * sinPitch - gy * cosPitch) * baseScale * perspective
                return Offset(sx, sy)
            }

            fun depthKey(gx: Float, gz: Float): Float {
                return gx * sinYaw + gz * cosYaw
            }

            // Ambient Deep Cyber Grid Backdrop
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0E213C), TacticalObsidian),
                    center = Offset(cx, cy),
                    radius = w.coerceAtLeast(h) * 0.7f
                )
            )

            // 3D Ground Coordinate Grid (-1.1 to +1.1)
            val gridSteps = 10
            for (i in -gridSteps..gridSteps) {
                val g = i / gridSteps.toFloat() * 1.15f
                val p1 = project3D(g, 0f, -1.15f)
                val p2 = project3D(g, 0f, 1.15f)
                val q1 = project3D(-1.15f, 0f, g)
                val q2 = project3D(1.15f, 0f, g)
                val alpha = if (i == 0) 0.36f else 0.14f
                drawLine(
                    color = NeonCyan.copy(alpha = alpha),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i == 0) 2f else 1f
                )
                drawLine(
                    color = NeonCyan.copy(alpha = alpha),
                    start = q1,
                    end = q2,
                    strokeWidth = if (i == 0) 2f else 1f
                )
            }

            // Tactical Laser Pathways Connecting Central Hub (Building 5) to All Buildings
            val hubCenter = project3D(0f, 0.01f, 0f)
            buildings.forEach { bld ->
                val bCenter = project3D(bld.gridX, 0.01f, bld.gridZ)
                val isSelectedPath = bld.id == selectedBuilding.id
                val pathColor = if (onlyWheelchairAccessible) {
                    TacticalGold
                } else if (isSelectedPath) {
                    NeonCyan
                } else {
                    bld.tacticalColor.copy(alpha = 0.45f)
                }

                drawLine(
                    color = pathColor,
                    start = hubCenter,
                    end = bCenter,
                    strokeWidth = if (isSelectedPath) 5f else 2.5f,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(18f, 12f),
                        phase = -laserFlowPhase
                    )
                )
            }

            // Sort buildings back-to-front by rotated Z depth so closer 3D buildings occlude farther ones
            val sortedBuildings = buildings.sortedBy { depthKey(it.gridX, it.gridZ) }
            hitboxes.clear()

            sortedBuildings.forEach { bld ->
                val isSelected = bld.id == selectedBuilding.id
                val hw = bld.widthUnits * 0.5f
                val hd = bld.depthUnits * 0.5f
                val floorHeightUnit = 0.11f

                val groundCenter = project3D(bld.gridX, 0f, bld.gridZ)
                hitboxes.add(
                    ProjectedBuildingHitbox(
                        building = bld,
                        screenCenter = groundCenter,
                        hitRadius = baseScale * 0.32f
                    )
                )

                // Selected Building Neon Glow Halo on Ground
                if (isSelected) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                bld.tacticalColor.copy(alpha = 0.52f * glowPulse),
                                Color.Transparent
                            ),
                            center = groundCenter,
                            radius = baseScale * 0.36f
                        ),
                        radius = baseScale * 0.36f,
                        center = groundCenter
                    )
                    drawCircle(
                        color = bld.tacticalColor.copy(alpha = 0.85f),
                        radius = baseScale * 0.24f * glowPulse,
                        center = groundCenter,
                        style = Stroke(width = 3f)
                    )
                }

                // Render Each Floor Slab (with Floor Slicing & Isolation support)
                val totalFloors = bld.floorsCount
                for (floorIdx in 0..totalFloors) {
                    val isIsolatedFloor = selectedFloorFilter != null && selectedFloorFilter == floorIdx
                    val isDimmedBySlice = selectedFloorFilter != null && selectedFloorFilter != floorIdx

                    // When floor slicing is active, vertically explode/separate higher floors
                    val sliceGap = if (selectedFloorFilter != null && floorIdx > selectedFloorFilter) {
                        0.14f
                    } else if (isIsolatedFloor) {
                        0.04f
                    } else {
                        0f
                    }

                    val yBottom = floorIdx * floorHeightUnit + sliceGap
                    val yTop = (floorIdx + 0.86f) * floorHeightUnit + sliceGap

                    // 4 corners bottom & top
                    val b0 = project3D(bld.gridX - hw, yBottom, bld.gridZ - hd)
                    val b1 = project3D(bld.gridX + hw, yBottom, bld.gridZ - hd)
                    val b2 = project3D(bld.gridX + hw, yBottom, bld.gridZ + hd)
                    val b3 = project3D(bld.gridX - hw, yBottom, bld.gridZ + hd)

                    val t0 = project3D(bld.gridX - hw, yTop, bld.gridZ - hd)
                    val t1 = project3D(bld.gridX + hw, yTop, bld.gridZ - hd)
                    val t2 = project3D(bld.gridX + hw, yTop, bld.gridZ + hd)
                    val t3 = project3D(bld.gridX - hw, yTop, bld.gridZ + hd)

                    val fillAlpha = when {
                        isIsolatedFloor -> 0.82f
                        isDimmedBySlice -> 0.10f
                        isSelected -> 0.48f
                        else -> 0.28f
                    }
                    val strokeAlpha = when {
                        isIsolatedFloor -> 1.0f
                        isDimmedBySlice -> 0.22f
                        isSelected -> 0.95f
                        else -> 0.62f
                    }

                    val baseColor = if (isIsolatedFloor) NeonCyan else bld.tacticalColor

                    // Draw 4 side walls of the floor slab
                    val faces = listOf(
                        listOf(b0, b1, t1, t0),
                        listOf(b1, b2, t2, t1),
                        listOf(b2, b3, t3, t2),
                        listOf(b3, b0, t0, t3)
                    )
                    faces.forEachIndexed { faceIdx, pts ->
                        val shadeFactor = when (faceIdx) {
                            0 -> 0.7f
                            1 -> 0.9f
                            2 -> 1.0f
                            else -> 0.8f
                        }
                        val wallPath = Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            lineTo(pts[1].x, pts[1].y)
                            lineTo(pts[2].x, pts[2].y)
                            lineTo(pts[3].x, pts[3].y)
                            close()
                        }
                        drawPath(
                            path = wallPath,
                            color = baseColor.copy(alpha = (fillAlpha * shadeFactor).coerceIn(0.05f, 0.9f))
                        )
                        drawPath(
                            path = wallPath,
                            color = baseColor.copy(alpha = strokeAlpha),
                            style = Stroke(width = if (isSelected || isIsolatedFloor) 2.2f else 1.2f)
                        )
                    }

                    // Roof / Floor Slab Top
                    val roofPath = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    }
                    drawPath(
                        path = roofPath,
                        color = baseColor.copy(alpha = (fillAlpha * 1.15f).coerceIn(0.08f, 0.95f))
                    )
                    drawPath(
                        path = roofPath,
                        color = baseColor.copy(alpha = strokeAlpha),
                        style = Stroke(width = if (isIsolatedFloor) 3f else 1.6f)
                    )

                    // Internal Room Partitions when Floor Slicing is active on this floor
                    if (isIsolatedFloor) {
                        val midA = Offset((t0.x + t1.x) / 2f, (t0.y + t1.y) / 2f)
                        val midB = Offset((t3.x + t2.x) / 2f, (t3.y + t2.y) / 2f)
                        val midC = Offset((t0.x + t3.x) / 2f, (t0.y + t3.y) / 2f)
                        val midD = Offset((t1.x + t2.x) / 2f, (t1.y + t2.y) / 2f)
                        drawLine(color = TacticalGold, start = midA, end = midB, strokeWidth = 2.2f)
                        drawLine(color = TacticalGold, start = midC, end = midD, strokeWidth = 2.2f)
                    }
                }

                // Draw Building Top Beacon & Floating Tactical Tag
                val topY = (totalFloors + 1.15f) * floorHeightUnit + (if (selectedFloorFilter != null) 0.14f else 0f)
                val roofCenter = project3D(bld.gridX, topY, bld.gridZ)

                if (isSelected) {
                    val beamTop = project3D(bld.gridX, topY + 0.35f, bld.gridZ)
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf( Color.Transparent, bld.tacticalColor)
                        ),
                        start = beamTop,
                        end = roofCenter,
                        strokeWidth = 4f
                    )
                    drawCircle(
                        color = bld.tacticalColor,
                        radius = 7f * glowPulse,
                        center = roofCenter
                    )
                }

                drawContext.canvas.nativeCanvas.apply {
                    labelPaint.color = if (isSelected) bld.tacticalColor.toArgb() else android.graphics.Color.WHITE
                    drawText(
                        bld.shortTitle,
                        roofCenter.x,
                        roofCenter.y - 24f,
                        labelPaint
                    )
                    val subInfo = if (showIotLiveOverlay) {
                        "${bld.floorsCount} طوابق • إشغال ${bld.crowdLoadPercent}%"
                    } else {
                        "${bld.floorsCount} طوابق • ${bld.entrancesCount} مداخل"
                    }
                    subLabelPaint.color = bld.tacticalColor.toArgb()
                    drawText(
                        subInfo,
                        roofCenter.x,
                        roofCenter.y - 2f,
                        subLabelPaint
                    )
                }
            }
        }

        // 2. Top Tactical HUD Bar (Mini-Radar + Camera/Layer Controls)
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

                // 3D Camera & Overlay Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(TacticalDeepNavy.copy(alpha = 0.88f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { autoOrbit = !autoOrbit },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("toggle_auto_orbit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "تدوير حر 360 درجة",
                            tint = if (autoOrbit) NeonEmerald else NeonCyan
                        )
                    }
                    IconButton(
                        onClick = { cameraZoom = (cameraZoom + 0.15f).coerceAtMost(1.85f) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "تكبير الحرم",
                            tint = TacticalTextPrimary
                        )
                    }
                    IconButton(
                        onClick = { cameraZoom = (cameraZoom - 0.15f).coerceAtLeast(0.68f) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "تصغير الحرم",
                            tint = TacticalTextPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            cameraYawDeg = 38f
                            cameraPitchDeg = 52f
                            cameraZoom = 1.05f
                            autoOrbit = false
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "إعادة ضبط الكاميرا",
                            tint = TacticalGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Floor Slicing & Isolation Bar (عزل الطوابق وتفكيك الطبقات)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(14.dp))
                    .background(TacticalDeepNavy.copy(alpha = 0.86f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("floor_slicing_bar")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "عزل الطوابق:",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )
                val floorOptions = listOf(
                    null to "كل الطوابق",
                    0 to "الأرضي G",
                    1 to "الأول 1",
                    2 to "الثاني 2",
                    3 to "الثالث 3",
                    4 to "الرابع 4"
                )
                floorOptions.forEach { (floorVal, label) ->
                    val selected = selectedFloorFilter == floorVal
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectFloorFilter(floorVal) },
                        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                            selectedLabelColor = NeonCyan,
                            containerColor = TacticalCardSurface,
                            labelColor = TacticalTextSecondary
                        )
                    )
                }

                FilterChip(
                    selected = onlyWheelchairAccessible,
                    onClick = onToggleWheelchair,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Accessible,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("ذوي الإعاقة", style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalGold.copy(alpha = 0.25f),
                        selectedLabelColor = TacticalGold,
                        containerColor = TacticalCardSurface,
                        labelColor = TacticalTextSecondary
                    )
                )
            }

            // Smart Academic Schedule Sync Notification (Section 7.3 SIS/LMS Integration)
            if (scheduleAlertVisible) {
                Spacer(modifier = Modifier.height(6.dp))
                val hall208 = halls.find { it.code == "#208" } ?: activeHall
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TacticalCardSurface.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TacticalGold.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .clickable {
                            onDismissScheduleAlert()
                            onLaunchAerialDrop(hall208)
                        }
                        .testTag("sis_schedule_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = TacticalGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مزامنة الجدول الأكاديمي: محاضرتك القادمة في #208 بعد 15 دقيقة - اضغط هنا لبدء الملاحة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TacticalTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Bottom Building Selector & Selected Building Tactical Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // 5 Buildings Horizontal Selector Strip
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                buildings.forEach { bld ->
                    val isSelected = bld.id == selectedBuilding.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) bld.tacticalColor.copy(alpha = 0.22f) else TacticalDeepNavy.copy(alpha = 0.9f),
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) bld.tacticalColor else bld.tacticalColor.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectBuilding(bld) }
                            .testTag("building_chip_${bld.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(bld.tacticalColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bld.shortTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) bld.tacticalColor else TacticalTextPrimary
                            )
                        }
                    }
                }
            }

            // Glassmorphic Selected Building & Quick Drop Card
            val buildingHalls = halls.filter { hall ->
                hall.buildingId == selectedBuilding.id &&
                    (selectedFloorFilter == null || hall.floorNumber == selectedFloorFilter)
            }.ifEmpty {
                halls.filter { it.buildingId == selectedBuilding.id }
            }
            val primaryHall = buildingHalls.firstOrNull() ?: activeHall

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = TacticalDeepNavy.copy(alpha = 0.94f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = selectedBuilding.tacticalColor.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("selected_building_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedBuilding.officialName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TacticalTextPrimary
                            )
                            Text(
                                text = "${selectedBuilding.floorsCount} طوابق | ${selectedBuilding.entrancesCount} مداخل رئيسية | كود ${selectedBuilding.colorName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = selectedBuilding.tacticalColor
                            )
                        }

                        Button(
                            onClick = { onLaunchAerialDrop(primaryHall) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = TacticalObsidian
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("campus_3d_drop_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightLand,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هبوط وملاحة ${primaryHall.code}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedBuilding.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hall chips inside this building
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        buildingHalls.forEach { hall ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TacticalCardSurface,
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (hall.isAvailableNow) NeonEmerald.copy(alpha = 0.6f) else TacticalGold.copy(alpha = 0.6f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onLaunchAerialDrop(hall) }
                                    .testTag("quick_hall_chip_${hall.rawNumber}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (hall.isAvailableNow) NeonEmerald else TacticalGold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${hall.code} ${hall.officialName} (${hall.floorLabel})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TacticalTextPrimary
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
