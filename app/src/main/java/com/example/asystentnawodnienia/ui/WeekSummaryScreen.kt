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

// Pokazujemy ekran podsumowania tygodnia
fun WeekSummaryScreen(navController: NavController, viewModel: WaterViewModel) {
    // Trzymamy datę, według której liczymy tydzień
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    // Trzymamy stan okna wyboru daty
    var showDatePicker by remember { mutableStateOf(false) }
    // Pokazujemy okno wyboru daty
    if (showDatePicker) {
        // Ustawiamy startowo wybraną datę
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Zapisujemy wybraną datę
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
            // Wyświetlamy górny pasek z nawigacją tygodni
            WeekSummaryTopBar(
                selectedDate = selectedDate,
                onPreviousWeek = { selectedDate = selectedDate.minusWeeks(1) },
                onNextWeek = { selectedDate = selectedDate.plusWeeks(1) },
                onDateClick = { showDatePicker = true },
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        // Pobieramy całą historię
        val allHistory = viewModel.getWeeklySummary()
        // Liczymy dane dla wybranego tygodnia
        val weeklyData = getWeekDataForDate(allHistory, selectedDate)

        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wyświetlamy komunikat, gdy nie ma danych
            if (weeklyData.values.sum() == 0) {
                Text("Brak danych dla wybranego tygodnia.")
            } else {
                // Wyświetlamy wykres tygodniowy
                WeeklyChart(weeklyData)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Budujemy górny pasek z zakresem tygodnia i przyciskami
fun WeekSummaryTopBar(
    selectedDate: LocalDate, onPreviousWeek: () -> Unit, onNextWeek: () -> Unit, onDateClick: () -> Unit, onBackClick: () -> Unit) {
    // Wyznaczamy początek tygodnia
    val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    // Wyznaczamy koniec tygodnia
    val endOfWeek = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    // Ustawiamy format dat w tytule
    val formatter = DateTimeFormatter.ofPattern("dd.MM")

    TopAppBar(
        // Pokazujemy zakres tygodnia
        title = { Text("${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}") },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wróć") } },
        actions = {
            // Przechodzimy do poprzedniego tygodnia
            IconButton(onClick = onPreviousWeek) { Icon(imageVector = Icons.Default.ArrowLeft, contentDescription = "Poprzedni tydzień") }
            // Otwieramy wybór daty
            IconButton(onClick = onDateClick) { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Wybierz datę") }
            // Przechodzimy do następnego tygodnia
            IconButton(onClick = onNextWeek) { Icon(imageVector = Icons.Default.ArrowRight, contentDescription = "Następny tydzień") }
        }
    )
}
// Liczymy sumy wody dla każdego dnia tygodnia
fun getWeekDataForDate(history: List<WaterIntake>, date: LocalDate): Map<LocalDate, Int> {
    // Ustalamy zakres tygodnia
    val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    // Tworzymy listę 7 dni tygodnia
    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    // Filtrujemy wpisy do wybranego tygodnia i sumujemy po dacie
    val historyForWeek = history
        .filter {
            val entryDate = LocalDate.parse(it.date)
            !entryDate.isBefore(startOfWeek) && !entryDate.isAfter(endOfWeek)
        }
        .groupBy { it.date }
        .mapValues { entry -> entry.value.sumOf { it.amountMl } }
// Zwracamy mapę: dzień -> suma ml
    return weekDates.associateWith { historyForWeek[it.toString()] ?: 0 }
}

@Composable
// Rysujemy prosty wykres słupkowy tygodnia
fun WeeklyChart(data: Map<LocalDate, Int>) {
    val entries = data.entries.toList()
    val maxIntake = entries.maxOfOrNull { it.value } ?: 1
    val maxValueBar = entries.maxByOrNull { it.value }
    val formatter = DateTimeFormatter.ofPattern("EEE")

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val animatables = remember(entries) { entries.map { Animatable(0f) } }
    val textMeasurer = rememberTextMeasurer()

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
            // Ustalamy szerokość słupków
            val barWidth = size.width / (entries.size * 2)
            // Zostawiamy miejsce na liczby nad słupkami
            val topPadding = 20.dp.toPx()
            val chartHeight = size.height - topPadding

            entries.forEachIndexed { index, entry ->
                // Liczymy wysokość słupka
                val barHeight = (entry.value.toFloat() / maxIntake.toFloat()) * chartHeight
                // Uwzględniamy animację
                val animatedBarHeight = barHeight * animatables[index].value
                // Wyróżniamy najwyższy słupek innym kolorem
                val barColor = if (entry == maxValueBar) secondaryColor else primaryColor
                val xOffset = index * barWidth * 2 + barWidth / 2
                // Rysujemy zaokrąglony słupek
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = xOffset, y = size.height - animatedBarHeight),
                    size = Size(width = barWidth, height = animatedBarHeight),
                    cornerRadius = CornerRadius(x = 10f, y = 10f)
                )
                // Pokazujemy wartość nad słupkiem pod koniec animacji
                if (animatables[index].value > 0.8f) {
                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString("${entry.value}"),
                        style = textStyle
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
        // Pokazujemy dni tygodnia pod wykresem
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            entries.forEach {
                Text(it.key.format(formatter), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}