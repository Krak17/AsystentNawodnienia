package com.example.asystentnawodnienia.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asystentnawodnienia.data.WaterIntake
import com.example.asystentnawodnienia.data.WaterRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel zarządza logiką UI i dostarcza dane widokom.
 * Komunikuje się wyłącznie z WaterRepository.
 */
class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    // Suma na dziś (obserwowana przez UI)
    private val _totalToday = MutableLiveData<Int>()
    val totalToday: LiveData<Int> = _totalToday

    // Historia spożycia wody (obserwowana przez UI)
    private val _history = MutableLiveData<List<WaterIntake>>()
    val history: LiveData<List<WaterIntake>> = _history

    /**
     * Dodaje ilość wypitej wody i aktualizuje sumę dzienną.
     */
    fun addWater(amount: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            repository.addWater(
                WaterIntake(
                    date = today,
                    amountMl = amount
                )
            )

            // Aktualizacja dziennego wyniku
            _totalToday.postValue(repository.getTotalForDay(today))

            // Odśwież historię (jeśli UI jej używa)
            _history.postValue(repository.getHistory())
        }
    }

    /**
     * Wczytuje sumę na dziś — np. przy starcie aplikacji.
     */
    fun refreshTodayTotal() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            _totalToday.postValue(repository.getTotalForDay(today))
        }
    }

    /**
     * Wczytuje historię wpisów.
     */
    fun loadHistory() {
        viewModelScope.launch {
            _history.postValue(repository.getHistory())
        }
    }
}
