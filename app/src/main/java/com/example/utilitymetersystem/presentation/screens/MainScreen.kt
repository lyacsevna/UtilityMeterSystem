package com.example.utilitymetersystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: UtilityViewModel) {
    val readings by viewModel.readings.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Показания счетчиков") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет показаний")
                }
            } else {
                ReadingList(readings = readings)
            }
        }
    }
}

@Composable
fun ReadingList(readings: List<UtilityReading>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(readings) { reading ->
            ReadingCard(reading = reading)
        }
    }
}

@Composable
fun ReadingCard(reading: UtilityReading) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = getUtilityTypeText(reading.type),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Значение: ${reading.value} ${getUtilityUnit(reading.type)}"
            )
            reading.note?.let { note ->
                Text(
                    text = "Заметка: $note",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Дата: ${reading.date}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun getUtilityTypeText(type: String): String {
    return when (type) {
        "WATER" -> "Вода"
        "ELECTRICITY" -> "Электричество"
        else -> "Неизвестный тип"
    }
}

private fun getUtilityUnit(type: String): String {
    return when (type) {
        "WATER" -> "м³"
        "ELECTRICITY" -> "кВт·ч"
        else -> ""
    }
}