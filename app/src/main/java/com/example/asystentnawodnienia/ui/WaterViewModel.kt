package com.example.asystentnawodnienia.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asystentnawodnienia.data.WaterIntake
import com.example.asystentnawodnienia.data.WaterRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _totalToday = mutableStateOf(0)
    val totalToday: State<Int> = _totalToday

    private val _history = mutableStateOf<List<WaterIntake>>(emptyList())
    val history: State<List<WaterIntake>> = _history

    // Nowy stan do obsługi wskaźnika ładowania
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _loading.value = true
            refreshTodayTotal()
            loadHistory()
            _loading.value = false
        }
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            _loading.value = true
            val today = LocalDate.now().toString()
            val waterIntake = WaterIntake(date = today, amountMl = amount)
            repository.addWater(waterIntake)
            refreshTodayTotal()
            loadHistory()
            _loading.value = false
        }
    }

    private fun refreshTodayTotal() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            _totalToday.value = repository.getTotalForDay(today)
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.getHistory()
        }
    }
}
