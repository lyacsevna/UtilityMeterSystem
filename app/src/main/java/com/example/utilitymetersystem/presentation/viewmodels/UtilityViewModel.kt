package com.example.utilitymetersystem.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.data.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UtilityViewModel @Inject constructor(
    private val repository: ReadingRepository
) : ViewModel() {

    private val _readings = MutableStateFlow<List<UtilityReading>>(emptyList())
    val readings: StateFlow<List<UtilityReading>> = _readings.asStateFlow()

    init {
        loadAllReadings()
    }

    private fun loadAllReadings() {
        viewModelScope.launch {
            repository.getAllReadings().collect { readingsList ->
                _readings.value = readingsList
            }
        }
    }

    fun addReading(type: UtilityType, value: Double, note: String?) {
        viewModelScope.launch {
            val reading = UtilityReading(
                type = type.name,
                value = value,
                note = note
            )
            repository.addReading(reading)
        }
    }
}