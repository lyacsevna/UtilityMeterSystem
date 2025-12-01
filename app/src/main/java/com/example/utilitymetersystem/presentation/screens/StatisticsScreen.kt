package com.example.utilitymetersystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController, viewModel: UtilityViewModel) {
    val statistics by remember { derivedStateOf { viewModel.getStatistics() } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Карточка общей статистики
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Общая статистика",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Вода
                    StatisticItem(
                        icon = Icons.Default.WaterDrop,
                        title = "Вода",
                        value = "${statistics.totalWater} м³",
                        subtitle = "${statistics.waterReadingsCount} показаний",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Электричество
                    StatisticItem(
                        icon = Icons.Default.FlashOn,
                        title = "Электричество",
                        value = "${statistics.totalElectricity} кВт·ч",
                        subtitle = "${statistics.electricityReadingsCount} показаний",
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Всего показаний
                    StatisticItem(
                        icon = Icons.Default.Receipt,
                        title = "Всего показаний",
                        value = statistics.periodReadings.toString(),
                        subtitle = "за весь период",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Информация о расчетах
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Как рассчитывается потребление?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Потребление рассчитывается как разница между последовательными показаниями\n" +
                                "• Учитываются только положительные разницы\n" +
                                "• Для точного расчета необходимо минимум 2 показания одного типа",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Советы по экономии
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Советы по экономии",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TipItem(
                        title = "Вода",
                        tips = listOf(
                            "• Устраните протечки кранов",
                            "• Используйте экономичные душевые головки",
                            "• Выключайте воду при чистке зубов"
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TipItem(
                        title = "Электричество",
                        tips = listOf(
                            "• Используйте LED-лампы",
                            "• Выключайте свет при выходе из комнаты",
                            "• Используйте энергоэффективную технику"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticItem(icon: ImageVector, title: String, value: String, subtitle: String, color: Color) {
    TODO("Not yet implemented")
}

@Composable
fun StatisticItem(icon: ImageVector, title: String, value: String, subtitle: String, color: Color) {
    TODO("Not yet implemented")
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TipItem(title: String, tips: List<String>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        tips.forEach { tip ->
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}