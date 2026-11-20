package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CampusBuilding
import com.example.model.CampusRepository
import com.example.model.FeasibilityMetric
import com.example.model.RoadmapMilestone
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalDeepNavy
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun FeasibilityInsightsScreen(
    buildings: List<CampusBuilding>,
    metrics: List<FeasibilityMetric> = CampusRepository.feasibilityMetrics,
    roadmap: List<RoadmapMilestone> = CampusRepository.roadmapMilestones,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalObsidian)
            .testTag("feasibility_insights_screen"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 6 Header: Strategic Feasibility & Operational Telemetry
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "التحليلات ودراسة الجدوى الاستراتيجية",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TacticalTextPrimary
                            )
                            Text(
                                text = "القياسات التشغيلية الذكية لحركة الحرم الجامعي وكفاءة الطاقة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // 4 Feasibility Metric Cards (-65% Transit Time, +78% Crowd Flow, 100% Accessibility, -38% Energy)
        items(metrics, key = { it.id }) { metric ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, metric.accentColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .testTag("feasibility_card_${metric.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = metric.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TacticalTextPrimary
                            )
                            Text(
                                text = metric.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = metric.accentColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = metric.accentColor.copy(alpha = 0.18f),
                            modifier = Modifier.border(1.dp, metric.accentColor, RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = metric.highlightValue,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = metric.accentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { metric.progressRatio },
                        color = metric.accentColor,
                        trackColor = TacticalCardSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = metric.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextSecondary
                    )
                }
            }
        }

        // Custom Canvas Chart: Green Campus Energy & Crowd Load per Building
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonEmerald.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مؤشرات الحرم الأخضر وتوازن الحشود في المباني الـ 5",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TacticalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val barCount = buildings.size
                        val spacing = size.width / (barCount * 2f + 1f)
                        buildings.forEachIndexed { idx, bld ->
                            val x = spacing + idx * spacing * 2f
                            val maxH = size.height - 24f
                            val crowdH = maxH * (bld.crowdLoadPercent / 100f)
                            val energyH = maxH * (bld.energySavingPercent / 100f)

                            // Crowd Load Bar
                            drawRoundRect(
                                color = bld.tacticalColor.copy(alpha = 0.85f),
                                topLeft = Offset(x, maxH - crowdH),
                                size = Size(spacing * 0.55f, crowdH),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // Energy Saving Bar
                            drawRoundRect(
                                color = NeonEmerald,
                                topLeft = Offset(x + spacing * 0.65f, maxH - energyH),
                                size = Size(spacing * 0.55f, energyH),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        buildings.forEach { bld ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "مبنى ${bld.id}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = bld.tacticalColor
                                )
                                Text(
                                    text = "وفر ${bld.energySavingPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Future Scope & Roadmap (خارطة الطريق للتطوير المستقبلي)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = TacticalGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "خارطة الطريق والابتكارات المتقدمة (Section 7)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TacticalTextPrimary
                )
            }
        }

        items(roadmap, key = { it.number }) { item ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, item.accentColor.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = item.accentColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = item.number,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = item.accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalTextPrimary
                                )
                                Text(
                                    text = item.englishSubtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = item.accentColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TacticalCardSurface
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.statusBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    item.bulletPoints.forEach { bullet ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(item.accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TacticalTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
