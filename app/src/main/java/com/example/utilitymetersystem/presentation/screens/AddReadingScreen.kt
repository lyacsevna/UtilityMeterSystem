package com.example.utilitymetersystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions


import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(navController: NavController, viewModel: UtilityViewModel) {
    var selectedType by remember { mutableStateOf(UtilityType.WATER) }
    var value by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Добавить показание") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Тип показания", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UtilityType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(
                            when (type) {
                                UtilityType.WATER -> "Вода"
                                UtilityType.ELECTRICITY -> "Электричество"
                            }
                        ) }
                    )
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Значение") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal
                ),
                placeholder = { Text(
                    when (selectedType) {
                        UtilityType.WATER -> "м³"
                        UtilityType.ELECTRICITY -> "кВт·ч"
                    }
                ) }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val numericValue = value.toDoubleOrNull()
                        if (numericValue != null) {
                            viewModel.addReading(selectedType, numericValue, note)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = value.isNotBlank() && value.toDoubleOrNull() != null
                ) {
                    Text("Сохранить")
                }

                Button(
                    onClick = {
                        navController.navigate("camera")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Сканировать камерой")
                }
            }
        }
    }
}