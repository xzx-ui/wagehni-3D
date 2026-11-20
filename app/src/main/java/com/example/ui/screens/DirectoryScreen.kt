package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CampusBuilding
import com.example.model.CampusHall
import com.example.model.HallCategory
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalDeepNavy
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun DirectoryScreen(
    buildings: List<CampusBuilding>,
    halls: List<CampusHall>,
    searchQuery: String,
    selectedCategory: HallCategory,
    onlyWheelchairAccessible: Boolean,
    bookedHallCodes: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (HallCategory) -> Unit,
    onToggleWheelchair: () -> Unit,
    onLaunchAerialDrop: (CampusHall) -> Unit,
    onBookHall: (String) -> Unit,
    onSpeakHallSummary: (CampusHall) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredHalls = halls.filter { hall ->
        val matchesCategory = selectedCategory == HallCategory.ALL || hall.category == selectedCategory
        val matchesWheelchair = !onlyWheelchairAccessible || hall.isWheelchairAccessible
        val q = searchQuery.trim()
        val matchesQuery = q.isEmpty() ||
            hall.code.contains(q, ignoreCase = true) ||
            hall.rawNumber.contains(q, ignoreCase = true) ||
            hall.officialName.contains(q, ignoreCase = true) ||
            hall.buildingName.contains(q, ignoreCase = true) ||
            hall.currentTopic.contains(q, ignoreCase = true) ||
            hall.specialtyKeywords.any { it.contains(q, ignoreCase = true) }
        matchesCategory && matchesWheelchair && matchesQuery
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalObsidian)
            .testTag("directory_screen_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner & Title
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_tactical_campus_hero),
                        contentDescription = "شعار دليل الحرم الجامعي التكتيكي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TacticalObsidian.copy(alpha = 0.62f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "دليل القاعات والمباني الذكي",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TacticalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "5 مبانٍ رئيسية • المسارات التفصيلية من البوابة إلى المقعد • حالة الإشغال اللحظية IoT",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        // Smart Search Bar (بالرقم مثل 402، أو التخصص مثل ذكاء اصطناعي، أمن سيبراني)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "ابحث بالرقم (مثل 402، 315) أو التخصص (ذكاء اصطناعي، أمن سيبراني)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = NeonCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح البحث",
                                tint = TacticalTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = NeonCyan.copy(alpha = 0.35f),
                    focusedContainerColor = TacticalDeepNavy,
                    unfocusedContainerColor = TacticalDeepNavy,
                    focusedTextColor = TacticalTextPrimary,
                    unfocusedTextColor = TacticalTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("directory_search_input")
            )
        }

        // Category Filter Chips (الكل، مؤتمرات، حاسب وتقنية، مدرجات كبرى، مالية وإدارة، بحث رقمي)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                HallCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category) },
                        label = {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.24f),
                            selectedLabelColor = NeonCyan,
                            containerColor = TacticalDeepNavy,
                            labelColor = TacticalTextSecondary
                        ),
                        modifier = Modifier.testTag("category_chip_${category.name}")
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
                    label = { Text("مسارات ذوي الإعاقة", style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalGold.copy(alpha = 0.25f),
                        selectedLabelColor = TacticalGold,
                        containerColor = TacticalDeepNavy,
                        labelColor = TacticalTextSecondary
                    )
                )
            }
        }

        // Section Header: Halls Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "القاعات والمختبرات المطابقة (${filteredHalls.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TacticalTextPrimary
                    )
                }
                Text(
                    text = "4 خطوات تفصيلية لكل مسار",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonEmerald
                )
            }
        }

        // Filtered Halls Cards
        items(filteredHalls, key = { it.code }) { hall ->
            val bldColor = buildings.find { it.id == hall.buildingId }?.tacticalColor ?: NeonCyan
            val isBooked = bookedHallCodes.contains(hall.code)

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, bldColor.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    .testTag("hall_card_${hall.rawNumber}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hall Code Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bldColor.copy(alpha = 0.2f),
                                modifier = Modifier.border(1.dp, bldColor, RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = hall.code,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = bldColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = hall.officialName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalTextPrimary
                                )
                                Text(
                                    text = "${hall.buildingName} • ${hall.floorLabel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TacticalTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { onSpeakHallSummary(hall) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TacticalCardSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "نطق بيانات القاعة",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Telemetry Chips (Capacity, Steps Count, Live Status)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Live Status Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hall.isAvailableNow) NeonEmerald.copy(alpha = 0.16f) else TacticalGold.copy(alpha = 0.16f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (hall.isAvailableNow) NeonEmerald else TacticalGold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = hall.liveStatusText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (hall.isAvailableNow) NeonEmerald else TacticalGold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TacticalCardSurface
                        ) {
                            Text(
                                text = "السعة: ${hall.capacityLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TacticalTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TacticalCardSurface
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${hall.steps.size} خطوات تفصيلية",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeonCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "النشاط الحالي: ${hall.currentTopic} (${hall.currentLecturer})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons: Drop & Navigate + Book Hall
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onLaunchAerialDrop(hall) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = TacticalObsidian
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("drop_and_navigate_${hall.rawNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightLand,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هبوط وملاحة 3D",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hall.isAvailableNow || isBooked) {
                            OutlinedButton(
                                onClick = { onBookHall(hall.code) },
                                enabled = !isBooked,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("book_hall_button_${hall.rawNumber}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventAvailable,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBooked) "محجوز لك" else "حجز مجاني",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: The 5 Topographical Campus Buildings Reference
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = TacticalGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الهيكل الطوبوغرافي للكلية (المباني الـ 5 الرئيسية)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TacticalTextPrimary
                )
            }
        }

        items(buildings, key = { "bld_${it.id}" }) { bld ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TacticalDeepNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, bld.tacticalColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bld.officialName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = bld.tacticalColor
                        )
                        Text(
                            text = "${bld.floorsCount} طوابق | ${bld.entrancesCount} مداخل",
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bld.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextSecondary
                    )
                }
            }
        }
    }
}
