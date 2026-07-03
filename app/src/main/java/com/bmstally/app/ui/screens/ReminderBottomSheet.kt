package com.bmstally.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ReminderStep { MAIN, AUTO_SUB }

@Composable
fun ReminderBottomSheet(
    onContinue: (String) -> Unit
) {
    var step by remember { mutableStateOf(ReminderStep.MAIN) }
    var mainSelection by remember { mutableStateOf<String?>(null) }
    var autoSubSelected by remember { mutableStateOf<Int?>(null) }

    val canContinue = when (mainSelection) {
        "Auto Reminders" -> autoSubSelected != null
        "Manual Reminders" -> true
        else -> false
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step == ReminderStep.AUTO_SUB) {
                IconButton(onClick = { step = ReminderStep.MAIN; autoSubSelected = null }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
            Text(
                if (step == ReminderStep.MAIN) "Select Type of Reminder" else "Auto Reminders",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        AnimatedContent(step) {
            when (it) {
                ReminderStep.MAIN -> MainStep(
                    mainSelection = mainSelection,
                    onAutoSelect = { mainSelection = "Auto Reminders"; step = ReminderStep.AUTO_SUB },
                    onManualSelect = { mainSelection = "Manual Reminders" }
                )
                ReminderStep.AUTO_SUB -> AutoSubStep(autoSubSelected) { autoSubSelected = it }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Reminders will be sent via email and SMS based on your configured schedule.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (mainSelection == "Manual Reminders") onContinue("manual")
                else onContinue("auto")
            },
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("CONTINUE")
        }
    }
}

@Composable
private fun MainStep(mainSelection: String?, onAutoSelect: () -> Unit, onManualSelect: () -> Unit) {
    Column {
        OptionCard(
            icon = Icons.Default.Autorenew,
            title = "Auto Reminders",
            description = "Automatically send reminders based on predefined rules and schedules.",
            isSelected = mainSelection == "Auto Reminders",
            showBadge = true,
            onClick = onAutoSelect
        )
        Spacer(Modifier.height(8.dp))
        OptionCard(
            icon = Icons.Default.Edit,
            title = "Manual Reminders",
            description = "Create and send reminders manually as needed.",
            isSelected = mainSelection == "Manual Reminders",
            onClick = onManualSelect
        )
    }
}

@Composable
private fun AutoSubStep(selected: Int?, onSelect: (Int) -> Unit) {
    Column {
        OptionCard(
            icon = Icons.Default.NotificationsActive,
            title = "Overdue Payment Reminder",
            description = "Send automatic reminders for payments that are past due date.",
            isSelected = selected == 0,
            showBadge = true,
            onClick = { onSelect(0) }
        )
        Spacer(Modifier.height(8.dp))
        OptionCard(
            icon = Icons.Default.Schedule,
            title = "Upcoming Payment Reminder",
            description = "Notify customers before payment due dates.",
            isSelected = selected == 1,
            onClick = { onSelect(1) }
        )
    }
}

@Composable
private fun OptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    showBadge: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF3F51B5)) else null
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = if (isSelected) Color(0xFF3F51B5) else Color.Gray)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.W600)
                    if (showBadge) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF4CAF50)) {
                            Text(
                                "Recommended",
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}