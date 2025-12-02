package com.example.asystentnawodnienia.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.asystentnawodnienia.data.WaterIntake
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSummaryScreen(navController: NavController, viewModel: WaterViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false 
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            WeekSummaryTopBar(
                selectedDate = selectedDate,
                onPreviousWeek = { selectedDate = selectedDate.minusWeeks(1) },
                onNextWeek = { selectedDate = selectedDate.plusWeeks(1) },
                onDateClick = { showDatePicker = true },
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        val allHistory = viewModel.getWeeklySummary()
        val weeklyData = getWeekDataForDate(allHistory, selectedDate)

        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weeklyData.values.sum() == 0) {
                Text("Brak danych dla wybranego tygodnia.")
            } else {
                WeeklyChart(weeklyData)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSummaryTopBar(
    selectedDate: LocalDate, onPreviousWeek: () -> Unit, onNextWeek: () -> Unit, onDateClick: () -> Unit, onBackClick: () -> Unit) {
    val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val formatter = DateTimeFormatter.ofPattern("dd.MM")

    TopAppBar(
        title = { Text("${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}") },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wróć") } },
        actions = {
            IconButton(onClick = onPreviousWeek) { Icon(imageVector = Icons.Default.ArrowLeft, contentDescription = "Poprzedni tydzień") }
            IconButton(onClick = onDateClick) { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Wybierz datę") }
            IconButton(onClick = onNextWeek) { Icon(imageVector = Icons.Default.ArrowRight, contentDescription = "Następny tydzień") }
        }
    )
}

fun getWeekDataForDate(history: List<WaterIntake>, date: LocalDate): Map<LocalDate, Int> {
    val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    val historyForWeek = history
        .filter { 
            val entryDate = LocalDate.parse(it.date)
            !entryDate.isBefore(startOfWeek) && !entryDate.isAfter(endOfWeek)
        }
        .groupBy { it.date }
        .mapValues { entry -> entry.value.sumOf { it.amountMl } }

    return weekDates.associateWith { historyForWeek[it.toString()] ?: 0 }
}

@Composable
fun WeeklyChart(data: Map<LocalDate, Int>) {
    val entries = data.entries.toList()
    val maxIntake = entries.maxOfOrNull { it.value } ?: 1
    val maxValueBar = entries.maxByOrNull { it.value }
    val formatter = DateTimeFormatter.ofPattern("EEE")

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val animatables = remember(entries) { entries.map { Animatable(0f) } }
    val textMeasurer = rememberTextMeasurer()

    // Tworzymy styl tekstu TUTAJ, w środowisku kompozycyjnym
    val textStyle = TextStyle(
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
    )

    LaunchedEffect(entries) {
        entries.forEachIndexed { index, _ ->
            launch {
                delay(index * 100L)
                animatables[index].animateTo(1f, tween(1000))
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) { 
            val barWidth = size.width / (entries.size * 2)

            entries.forEachIndexed { index, entry ->
                val barHeight = (entry.value.toFloat() / maxIntake.toFloat()) * size.height
                val animatedBarHeight = barHeight * animatables[index].value

                val barColor = if (entry == maxValueBar) secondaryColor else primaryColor
                val xOffset = index * barWidth * 2 + barWidth / 2

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = xOffset, y = size.height - animatedBarHeight),
                    size = Size(width = barWidth, height = animatedBarHeight),
                    cornerRadius = CornerRadius(x = 10f, y = 10f)
                )

                if (animatables[index].value > 0.8f) {
                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString("${entry.value}"),
                        style = textStyle // Używamy pre-komponowanego stylu
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = xOffset + barWidth / 2 - textLayoutResult.size.width / 2,
                            y = size.height - animatedBarHeight - textLayoutResult.size.height - 5.dp.toPx()
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            entries.forEach {
                Text(it.key.format(formatter), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
