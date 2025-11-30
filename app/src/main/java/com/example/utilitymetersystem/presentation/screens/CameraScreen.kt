package com.example.utilitymetersystem.presentation.screens

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController, viewModel: UtilityViewModel) {
    var recognizedText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Ланчер для запроса разрешения камеры
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("CameraScreen", "Разрешение на камеру получено")
        } else {
            Log.d("CameraScreen", "Разрешение на камеру отклонено")
        }
    }

    // Проверяем разрешение при запуске
    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Запрашиваем разрешение если его нет
    LaunchedEffect(Unit) {
        if (!hasCameraPermission.value) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Сканирование показаний") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (recognizedText.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val number = extractNumberFromText(recognizedText) ?: 0.0
                        viewModel.addReading(
                            type = UtilityType.WATER,
                            value = number,
                            note = "Считано камерой: $recognizedText"
                        )
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Использовать")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission.value) {
                // Простая камера без распознавания текста (для начала)
                SimpleCameraView(modifier = Modifier.weight(1f))

                // Поле для ручного ввода (временно)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Временный режим",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Камера работает! Распознавание текста будет добавлено позже.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                // Временная заглушка - добавляем тестовые данные
                                viewModel.addReading(
                                    type = UtilityType.WATER,
                                    value = 123.45,
                                    note = "Тестовые данные с камеры"
                                )
                                navController.popBackStack()
                            }
                        ) {
                            Text("Добавить тестовые показания")
                        }
                    }
                }
            } else {
                // Если нет разрешения
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📷 Требуется доступ к камере",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Для сканирования показаний счетчика необходимо разрешение на использование камеры",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        ) {
                            Text("Запросить разрешение")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleCameraView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                // Инициализируем камеру
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(surfaceProvider)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        // Останавливаем предыдущие use cases и запускаем новые
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )

                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Ошибка инициализации камеры: ${e.message}", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        },
        modifier = modifier
    )
}

// Функция для извлечения чисел из текста
private fun extractNumberFromText(text: String): Double? {
    val regex = """\d+[,.]?\d*""".toRegex()
    val match = regex.find(text)
    return match?.value?.replace(',', '.')?.toDoubleOrNull()
}