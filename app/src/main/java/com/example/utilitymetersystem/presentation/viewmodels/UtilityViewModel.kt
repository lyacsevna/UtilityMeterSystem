package com.example.utilitymetersystem.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.data.repository.UtilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UtilityViewModel @Inject constructor(
    private val repository: UtilityRepository
) : ViewModel() {

    val waterReadings = repository.getReadingsByType(UtilityType.WATER)
    val electricityReadings = repository.getReadingsByType(UtilityType.ELECTRICITY)
    val allReadings = repository.getAllReadings()

    private val _currentReading = MutableStateFlow<UtilityReading?>(null)
    val currentReading: StateFlow<UtilityReading?> = _currentReading.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    fun setCurrentReading(reading: UtilityReading?) {
        _currentReading.value = reading
    }

    fun setScanResult(result: String?) {
        _scanResult.value = result
    }

    fun addReading(type: UtilityType, value: Double, note: String = "") {
        viewModelScope.launch {
            val reading = UtilityReading(
                type = type,
                value = value,
                date = Date(),
                note = note
            )
            repository.insertReading(reading)
        }
    }

    fun updateReading(reading: UtilityReading) {
        viewModelScope.launch {
            repository.updateReading(reading)
        }
    }

    fun deleteReading(reading: UtilityReading) {
        viewModelScope.launch {
            repository.deleteReading(reading)
        }
    }

    fun loadReading(id: Long) {
        viewModelScope.launch {
            val reading = repository.getReadingById(id)
            _currentReading.value = reading
        }
    }
}