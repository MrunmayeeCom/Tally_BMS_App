package com.bmstally.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bmstally.app.model.MonthlySummary
import com.bmstally.app.viewmodel.MonthlySummaryViewModel
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    onBack: () -> Unit,
    viewModel: MonthlySummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Summary", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(12.dp)
        ) {
            YearSelector(state.selectedYear, viewModel.years) { viewModel.setYear(it) }

            Spacer(Modifier.height(12.dp))
            SummaryCardsGrid(state)
            if (state.previousYearTotals != null) {
                PreviousYearComparison(state)
            }
            Spacer(Modifier.height(16.dp))
            if (state.data.isNotEmpty()) {
                BarChartCard(state.data)
                Spacer(Modifier.height(12.dp))
                LineChartCard(state.data)
                Spacer(Modifier.height(12.dp))
                MonthlyTable(state.data)
            } else {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No data available", color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearSelector(selectedYear: Int, years: List<Int>, onYearSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("Select Year") },
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = { onYearSelected(year); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SummaryCardsGrid(state: com.bmstally.app.viewmodel.MonthlySummaryState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCard("Total Turnover", "₹${formatLakh(state.totalTurnover)}", Icons.Default.TrendingUp, Color(0xFF3F51B5), Modifier.weight(1f))
        SummaryCard("Total Expense", "₹${formatLakh(state.totalExpense)}", Icons.Default.TrendingDown, Color(0xFFE53935), Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCard("Total Profit", "₹${formatLakh(state.totalProfit)}", Icons.Default.TrendingUp, if (state.totalProfit >= 0) Color(0xFF4CAF50) else Color(0xFFE53935), Modifier.weight(1f))
        SummaryCard("Profit Margin", "${String.format("%.2f", state.profitMargin)}%", Icons.Default.PieChart, Color(0xFFFF9800), Modifier.weight(1f), isPercentage = true)
    }
}

@Composable
private fun SummaryCard(
    title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color, modifier: Modifier = Modifier, isPercentage: Boolean = false
) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.extraLarge, color = color.copy(alpha = 0.1f)) {
                Icon(icon, null, Modifier.padding(8.dp).size(24.dp), tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            }
        }
    }
}

@Composable
private fun PreviousYearComparison(state: com.bmstally.app.viewmodel.MonthlySummaryState) {
    val (turnover, expense, profit) = state.previousYearTotals ?: return
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("vs ${state.selectedYear - 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
            Spacer(Modifier.height(4.dp))
            Text("Turnover: ₹${formatLakh(turnover)}  |  Expense: ₹${formatLakh(expense)}  |  Profit: ₹${formatLakh(profit)}", fontSize = 11.sp, color = Color(0xFF424242))
        }
    }
}

@Composable
private fun BarChartCard(data: List<MonthlySummary>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Monthly Turnover vs Expense", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val maxVal = data.maxOf { maxOf(it.turnover, it.expense) }
            val density = LocalDensity.current
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = with(density) { 9.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                val chartW = size.width
                val chartH = size.height - 30f
                val barW = chartW / (data.size * 3) * 2
                val gap = chartW / (data.size * 3)

                data.forEachIndexed { i, m ->
                    val x = i * (barW + gap * 2) + gap
                    val tH = (m.turnover / maxVal).toFloat() * chartH * 0.85f
                    val eH = (m.expense / maxVal).toFloat() * chartH * 0.85f

                    drawRoundRect(Color(0xFF3F51B5), topLeft = Offset(x, chartH - tH), size = androidx.compose.ui.geometry.Size(barW, tH))
                    drawRoundRect(Color(0xFFE53935), topLeft = Offset(x + barW + gap, chartH - eH), size = androidx.compose.ui.geometry.Size(barW, eH))
                }

                data.forEachIndexed { i, m ->
                    val x = i * (barW + gap * 2) + gap + barW / 2
                    drawContext.canvas.nativeCanvas.drawText(
                        m.month.take(3), x, size.height, textPaint
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF3F51B5), modifier = Modifier.size(12.dp)) {}
                Spacer(Modifier.width(4.dp)); Text("Turnover", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.width(16.dp))
                Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFE53935), modifier = Modifier.size(12.dp)) {}
                Spacer(Modifier.width(4.dp)); Text("Expense", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun LineChartCard(data: List<MonthlySummary>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Profit Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val profits = data.map { it.profit }
            val minP = profits.min()
            val maxP = profits.max()
            val range = maxOf(maxP - minP, 1.0)
            val density = LocalDensity.current
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = with(density) { 9.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                val chartW = size.width
                val chartH = size.height - 30f
                val step = chartW / (data.size - 1).coerceAtLeast(1)

                val path = Path()
                data.forEachIndexed { i, m ->
                    val x = i * step
                    val y = chartH - ((m.profit - minP) / range).toFloat() * chartH * 0.85f - 10f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(path, Color(0xFF4CAF50), style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                data.forEachIndexed { i, m ->
                    val x = i * step
                    val y = chartH - ((m.profit - minP) / range).toFloat() * chartH * 0.85f - 10f
                    drawCircle(Color(0xFF4CAF50), radius = 4f, center = Offset(x, y))
                }

                val midIdx = data.size / 2
                data.forEachIndexed { i, m ->
                    if (i == 0 || i == data.lastIndex || i == midIdx) {
                        val x = i * step
                        drawContext.canvas.nativeCanvas.drawText(
                            m.month.take(3), x, size.height, textPaint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTable(data: List<MonthlySummary>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Monthly Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Month", fontWeight = FontWeight.W600, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1.2f))
                Text("Turnover", fontWeight = FontWeight.W600, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Expense", fontWeight = FontWeight.W600, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Profit", fontWeight = FontWeight.W600, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Margin", fontWeight = FontWeight.W600, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
            HorizontalDivider()
            data.forEach { m ->
                val margin = if (m.turnover > 0) (m.profit / m.turnover) * 100 else 0.0
                val profitColor = if (m.profit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(m.month, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text(formatCompact(m.turnover), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(formatCompact(m.expense), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(formatCompact(m.profit), fontSize = 12.sp, color = profitColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("${String.format("%.1f", margin)}%", fontSize = 12.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
                Text(formatCompact(data.sumOf { it.turnover }), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(formatCompact(data.sumOf { it.expense }), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(formatCompact(data.sumOf { it.profit }), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (data.sumOf { it.profit } >= 0) Color(0xFF2E7D32) else Color(0xFFC62828), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                val totalMargin = if (data.sumOf { it.turnover } > 0) (data.sumOf { it.profit } / data.sumOf { it.turnover }) * 100 else 0.0
                Text("${String.format("%.1f", totalMargin)}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
        }
    }
}

private fun formatCompact(value: Double): String {
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_00_00_000 -> "₹${String.format("%.1f", value / 1_00_00_000)}Cr"
        abs >= 1_00_000 -> "₹${String.format("%.1f", value / 1_00_000)}L"
        abs >= 1_000 -> "₹${String.format("%.1f", value / 1_000)}K"
        else -> "₹${String.format("%.0f", value)}"
    }
}

private fun formatLakh(value: Double): String {
    return "${String.format("%.2f", value / 100000)}L"
}
