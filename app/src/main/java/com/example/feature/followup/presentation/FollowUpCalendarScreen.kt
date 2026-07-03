package com.example.feature.followup.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feature.followup.domain.FollowUp
import com.example.feature.followup.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpCalendarScreen(
    viewModel: FollowUpCalendarViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val rawMode by viewModel.calendarMode.collectAsState()

    // Interactive date focus for calendar clicks
    var selectedDateStr by remember { mutableStateOf("2026-06-20") } // Matches current context pre-fills

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow-Up Calendar & Slots") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calendar_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Segmented mode picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Daily", "Weekly", "Monthly").forEach { m ->
                        val isSel = rawMode == m
                        Button(
                            onClick = { viewModel.setMode(m) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("calendar_mode_btn_$m"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(m, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Calendar slots content
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Timeline Engine Offline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(state.message, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    is UiState.Success -> {
                        val grouped = state.data
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            when (rawMode) {
                                "Daily" -> DailyCalendarView(
                                    grouped = grouped,
                                    selectedDate = selectedDateStr,
                                    onSelectDate = { selectedDateStr = it },
                                    onNavigateToDetail = onNavigateToDetail
                                )
                                "Weekly" -> WeeklyCalendarView(
                                    grouped = grouped,
                                    onNavigateToDetail = onNavigateToDetail
                                )
                                "Monthly" -> MonthlyCalendarView(
                                    grouped = grouped,
                                    selectedDate = selectedDateStr,
                                    onSelectDate = { selectedDateStr = it },
                                    onNavigateToDetail = onNavigateToDetail
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================== Mode 1: Daily slot planner lists ======================
@Composable
fun DailyCalendarView(
    grouped: Map<String, List<FollowUp>>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // Show horizontal date strip around current date
    val dates = listOf("2026-06-19", "2026-06-20", "2026-06-21", "2026-06-22", "2026-06-23", "2026-06-24", "2026-06-25")
    val daysLabel = listOf("Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu")

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("daily_calendar_strip"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dates.size) { idx ->
                val dateStr = dates[idx]
                val dayLabel = daysLabel[idx]
                val dayNum = dateStr.substring(8)
                val isSel = selectedDate == dateStr
                val hasItems = grouped[dateStr]?.isNotEmpty() ?: false

                Card(
                    modifier = Modifier
                        .width(55.dp)
                        .clickable { onSelectDate(dateStr) }
                        .testTag("day_cell_${dateStr}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dayLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dayNum, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (hasItems) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE65100))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        PaddingValues(horizontal = 16.dp).let {
            Text(
                "Tasks Scheduled for $selectedDate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(it)
            )
        }

        val itemsForDate = grouped[selectedDate] ?: emptyList()

        if (itemsForDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No interactions scheduled today", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("daily_timeline_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itemsForDate) { item ->
                    CalendarCardItem(item = item, onClick = { onNavigateToDetail(item.id) })
                }
            }
        }
    }
}

// ====================== Mode 2: Weekly expandables ======================
@Composable
fun WeeklyCalendarView(
    grouped: Map<String, List<FollowUp>>,
    onNavigateToDetail: (String) -> Unit
) {
    // 7 days of the core week
    val weekDays = listOf(
        Pair("2026-06-19", "Friday"),
        Pair("2026-06-20", "Saturday (Today)"),
        Pair("2026-06-21", "Sunday"),
        Pair("2026-06-22", "Monday"),
        Pair("2026-06-23", "Tuesday"),
        Pair("2026-06-24", "Wednesday"),
        Pair("2026-06-25", "Thursday")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("weekly_calendar_scroll"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(weekDays) { day ->
            val dateStr = day.first
            val labelStr = day.second
            val tasks = grouped[dateStr] ?: emptyList()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(labelStr, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tasks.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "${tasks.size} Actions Scheduled",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (tasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        tasks.forEach { item ->
                            CalendarCardRow(item = item, onClick = { onNavigateToDetail(item.id) })
                        }
                    }
                }
            }
        }
    }
}

// ====================== Mode 3: Month matrix cells ======================
@Composable
fun MonthlyCalendarView(
    grouped: Map<String, List<FollowUp>>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // Generate static matrix for June 2026 for complete multi-tenant sync validation
    val daysInJune = 30
    val startDayOffset = 0 // June 1st, 2026 is Monday

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "June 2026",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Weekday labels
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid cells block - Static Month calculation
        Column(modifier = Modifier.padding(horizontal = 6.dp)) {
            var currentDay = 1
            for (row in 0..4) { // 5 rows
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        if (currentDay > daysInJune) {
                            Box(modifier = Modifier.weight(1f)) {}
                        } else {
                            val formattedDay = String.format("%02d", currentDay)
                            val targetDateStr = "2026-06-$formattedDay"
                            val isFocused = selectedDate == targetDateStr
                            val tasks = grouped[targetDateStr] ?: emptyList()
                            val localDay = currentDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isFocused) 1.5.dp else 0.5.dp,
                                        color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectDate(targetDateStr) }
                                    .testTag("month_cell_$localDay"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$localDay",
                                        fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (tasks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE65100))
                                        )
                                    }
                                }
                            }
                            currentDay++
                        }
                    }
                }
            }
        }

        // Sub-list for selected Day items
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Focused Actions: $selectedDate",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        val selectedTasks = grouped[selectedDate] ?: emptyList()

        if (selectedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No follow-ups targeted on this date.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("monthly_focused_timeline"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedTasks) { item ->
                    CalendarCardItem(item = item, onClick = { onNavigateToDetail(item.id) })
                }
            }
        }
    }
}

// ====================== Sub cards and visual components ======================
@Composable
fun CalendarCardItem(item: FollowUp, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("calendar_item_row_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("${item.type}  |  Priority: ${item.priority}  |  ${item.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Text("Due Today", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
        }
    }
}

@Composable
fun CalendarCardRow(item: FollowUp, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    when (item.priority) {
                        "High" -> Color.Red
                        "Medium" -> Color(0xFFEF6C00)
                        else -> Color.Gray
                    }
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.customerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${item.type} Directives: '${item.notes}'", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(item.status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}
