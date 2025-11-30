package com.example.utilitymetersystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: UtilityViewModel) {
    val waterReadings by viewModel.waterReadings.collectAsState(initial = emptyList())
    val electricityReadings by viewModel.electricityReadings.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Учет коммунальных услуг") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_reading") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить показание")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Статистика
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Вода",
                    value = waterReadings.firstOrNull()?.value?.toString() ?: "0",
                    unit = "м³"
                )
                StatCard(
                    title = "Электричество",
                    value = electricityReadings.firstOrNull()?.value?.toString() ?: "0",
                    unit = "кВт·ч"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Последние показания
            Text(
                text = "Последние показания",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (waterReadings.isNotEmpty() || electricityReadings.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(waterReadings.take(3) + electricityReadings.take(3)) { reading ->
                        ReadingItem(reading = reading) {
                            viewModel.setCurrentReading(reading)
                            navController.navigate("edit_reading")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нет данных о показаниях")
                    Text("Нажмите + чтобы добавить первое показание")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Вся история")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(unit, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ReadingItem(reading: com.example.utilitymetersystem.data.models.UtilityReading, onEdit: () -> Unit) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (reading.type) {
                        UtilityType.WATER -> "Вода"
                        UtilityType.ELECTRICITY -> "Электричество"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(reading.date),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (reading.note.isNotBlank()) {
                    Text(
                        text = reading.note,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "${reading.value} ${
                    when (reading.type) {
                        UtilityType.WATER -> "м³"
                        UtilityType.ELECTRICITY -> "кВт·ч"
                    }
                }",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}