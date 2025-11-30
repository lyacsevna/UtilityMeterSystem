package com.example.utilitymetersystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.utilitymetersystem.data.models.UtilityReading
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: UtilityViewModel) {
    val readings = viewModel.readings.value

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
                text = when (reading.type) {
                    com.example.utilitymetersystem.data.models.UtilityType.WATER -> "Вода"
                    com.example.utilitymetersystem.data.models.UtilityType.ELECTRICITY -> "Электричество"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Значение: ${reading.value} ${
                    when (reading.type) {
                        com.example.utilitymetersystem.data.models.UtilityType.WATER -> "м³"
                        com.example.utilitymetersystem.data.models.UtilityType.ELECTRICITY -> "кВт·ч"
                    }
                }"
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