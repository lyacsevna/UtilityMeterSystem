package com.example.utilitymetersystem.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.models.UtilityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class UtilityViewModel : ViewModel() {

    private val _readings = MutableStateFlow<List<UtilityReading>>(emptyList())
    val readings: StateFlow<List<UtilityReading>> = _readings.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _filterType = MutableStateFlow<UtilityType?>(null)
    val filterType: StateFlow<UtilityType?> = _filterType.asStateFlow()

    fun addReading(type: UtilityType, value: Double, note: String = "", imageUri: String? = null) {
        viewModelScope.launch {
            val newReading = UtilityReading(
                type = type,
                value = value,
                note = note,
                imageUri = imageUri
            )

            // Проверка на корректность показания
            if (!newReading.isValidForType()) {
                throw IllegalArgumentException("Некорректное значение для типа $type")
            }

            // Проверка что новое показание не меньше предыдущего
            val lastReading = getLastReadingForType(type)
            if (lastReading != null && value < lastReading.value) {
                throw IllegalArgumentException("Новое показание не может быть меньше предыдущего")
            }

            val updatedList = _readings.value + newReading
            _readings.value = applySortingAndFiltering(updatedList)
        }
    }

    fun updateReading(updatedReading: UtilityReading) {
        viewModelScope.launch {
            val currentList = _readings.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == updatedReading.id }

            if (index != -1) {
                // Проверка на корректность
                if (!updatedReading.isValidForType()) {
                    throw IllegalArgumentException("Некорректное значение для типа ${updatedReading.type}")
                }

                // Проверка что новое показание не меньше предыдущего для этого типа
                val previousReadings = currentList
                    .filter { it.type == updatedReading.type && it.id != updatedReading.id }
                    .sortedBy { it.date }

                val previousReading = previousReadings.lastOrNull { it.date < updatedReading.date }
                if (previousReading != null && updatedReading.value < previousReading.value) {
                    throw IllegalArgumentException("Показание не может быть меньше предыдущего")
                }

                currentList[index] = updatedReading
                _readings.value = applySortingAndFiltering(currentList)
            }
        }
    }

    fun deleteReading(readingId: String) {
        viewModelScope.launch {
            val updatedList = _readings.value.filter { it.id != readingId }
            _readings.value = applySortingAndFiltering(updatedList)
        }
    }

    fun setSortType(sortType: SortType) {
        viewModelScope.launch {
            _sortType.value = sortType
            _readings.value = applySortingAndFiltering(_readings.value)
        }
    }

    fun setFilterType(type: UtilityType?) {
        viewModelScope.launch {
            _filterType.value = type
            _readings.value = applySortingAndFiltering(_readings.value)
        }
    }

    fun clearFilter() {
        viewModelScope.launch {
            _filterType.value = null
            _readings.value = applySortingAndFiltering(_readings.value)
        }
    }

    private fun applySortingAndFiltering(list: List<UtilityReading>): List<UtilityReading> {
        var result = list

        // Применяем фильтр по типу
        _filterType.value?.let { filterType ->
            result = result.filter { it.type == filterType }
        }

        // Применяем сортировку
        result = when (_sortType.value) {
            SortType.DATE_ASC -> result.sortedBy { it.date }
            SortType.DATE_DESC -> result.sortedByDescending { it.date }
            SortType.TYPE -> result.sortedBy { it.type }
            SortType.VALUE_ASC -> result.sortedBy { it.value }
            SortType.VALUE_DESC -> result.sortedByDescending { it.value }
        }

        return result
    }

    private fun getLastReadingForType(type: UtilityType): UtilityReading? {
        return _readings.value
            .filter { it.type == type }
            .maxByOrNull { it.date }
    }

    // Статистика
    fun getStatistics(startDate: Date? = null, endDate: Date? = null): Statistics {
        val filteredReadings = _readings.value.filter { reading ->
            (startDate == null || reading.date >= startDate) &&
                    (endDate == null || reading.date <= endDate)
        }

        val waterReadings = filteredReadings.filter { it.type == UtilityType.WATER }
        val electricityReadings = filteredReadings.filter { it.type == UtilityType.ELECTRICITY }

        val totalWater = calculateTotalUsage(waterReadings)
        val totalElectricity = calculateTotalUsage(electricityReadings)

        return Statistics(
            totalWater = totalWater,
            totalElectricity = totalElectricity,
            waterReadingsCount = waterReadings.size,
            electricityReadingsCount = electricityReadings.size,
            periodReadings = filteredReadings.size
        )
    }

    private fun calculateTotalUsage(readings: List<UtilityReading>): Double {
        if (readings.size < 2) return 0.0

        val sortedReadings = readings.sortedBy { it.date }
        var total = 0.0

        for (i in 1 until sortedReadings.size) {
            val difference = sortedReadings[i].value - sortedReadings[i - 1].value
            if (difference > 0) {
                total += difference
            }
        }

        return total
    }
}

data class Statistics(
    val totalWater: Double,
    val totalElectricity: Double,
    val waterReadingsCount: Int,
    val electricityReadingsCount: Int,
    val periodReadings: Int
)

enum class SortType {
    DATE_ASC, DATE_DESC, TYPE, VALUE_ASC, VALUE_DESC
}