package com.example.utilitymetersystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.presentation.viewmodels.SortType
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: UtilityViewModel) {
    val readings by viewModel.readings.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var readingToEdit by remember { mutableStateOf<com.example.utilitymetersystem.data.models.UtilityReading?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("История показаний") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка фильтра
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Фильтр",
                            tint = if (filterType != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Кнопка сортировки
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                    }
                    // Кнопка статистики
                    IconButton(onClick = { navController.navigate("statistics") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Статистика")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Информация о фильтрах
            if (filterType != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Фильтр: ${getUtilityTypeText(filterType!!)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearFilter() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить фильтр")
                        }
                    }
                }
            }

            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Нет данных",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Нет показаний",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(readings, key = { it.id }) { reading ->
                        ReadingCard(
                            reading = reading,
                            onEdit = {
                                readingToEdit = reading
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteReading(reading.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Диалог сортировки
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Сортировка") },
            text = {
                Column {
                    SortType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortType == type,
                                onClick = {
                                    viewModel.setSortType(type)
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getSortTypeText(type),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Диалог фильтра
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Фильтр по типу") },
            text = {
                Column {
                    UtilityType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = filterType == type,
                                onClick = {
                                    viewModel.setFilterType(type)
                                    showFilterDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getUtilityTypeText(type),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    // Опция "Все"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filterType == null,
                            onClick = {
                                viewModel.clearFilter()
                                showFilterDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Все типы",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Диалог редактирования
    if (showEditDialog && readingToEdit != null) {
        EditReadingDialog(
            reading = readingToEdit!!,
            onDismiss = {
                showEditDialog = false
                readingToEdit = null
            },
            onSave = { updatedReading ->
                viewModel.updateReading(updatedReading)
                showEditDialog = false
                readingToEdit = null
            }
        )
    }
}

@Composable
fun ReadingCard(
    reading: com.example.utilitymetersystem.data.models.UtilityReading,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (reading.type) {
                UtilityType.WATER -> MaterialTheme.colorScheme.primaryContainer
                UtilityType.ELECTRICITY -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getUtilityTypeText(reading.type),
                    style = MaterialTheme.typography.titleMedium,
                    color = when (reading.type) {
                        UtilityType.WATER -> MaterialTheme.colorScheme.primary
                        UtilityType.ELECTRICITY -> MaterialTheme.colorScheme.secondary
                    }
                )
                Text(
                    text = "${reading.value} ${getUnitForType(reading.type)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(reading.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (reading.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reading.note,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EditReadingDialog(
    reading: com.example.utilitymetersystem.data.models.UtilityReading,
    onDismiss: () -> Unit,
    onSave: (com.example.utilitymetersystem.data.models.UtilityReading) -> Unit
) {
    var valueText by remember { mutableStateOf(reading.value.toString()) }
    var noteText by remember { mutableStateOf(reading.note) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать показание") },
        text = {
            Column {
                Text(
                    text = "Тип: ${getUtilityTypeText(reading.type)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = valueText,
                    onValueChange = {
                        valueText = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Показание") },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Заметка") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val newValue = valueText.replace(",", ".").toDouble()
                        val updatedReading = reading.copy(
                            value = newValue,
                            note = noteText
                        )
                        onSave(updatedReading)
                    } catch (e: NumberFormatException) {
                        errorMessage = "Введите корректное число"
                    } catch (e: IllegalArgumentException) {
                        errorMessage = e.message
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun getSortTypeText(sortType: SortType): String {
    return when (sortType) {
        SortType.DATE_ASC -> "Дата (старые сначала)"
        SortType.DATE_DESC -> "Дата (новые сначала)"
        SortType.TYPE -> "Тип счетчика"
        SortType.VALUE_ASC -> "Показание (по возрастанию)"
        SortType.VALUE_DESC -> "Показание (по убыванию)"
    }
}

private fun getUnitForType(type: UtilityType): String {
    return when (type) {
        UtilityType.WATER -> "м³"
        UtilityType.ELECTRICITY -> "кВт·ч"
    }
}

private fun getUtilityTypeText(type: UtilityType): String {
    return when (type) {
        UtilityType.WATER -> "Вода"
        UtilityType.ELECTRICITY -> "Электричество"
    }
}