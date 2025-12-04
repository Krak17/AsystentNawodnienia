package com.example.asystentnawodnienia.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.asystentnawodnienia.data.SettingsManager

/**
 * Fabryka do tworzenia instancji SettingsViewModel.
 */
class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            // Tworzymy SettingsManager, który jest potrzebny dla ViewModelu
            val settingsManager = SettingsManager(context)
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
