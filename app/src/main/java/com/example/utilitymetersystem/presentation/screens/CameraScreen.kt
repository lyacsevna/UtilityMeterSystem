package com.example.utilitymetersystem.presentation.screens

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController, viewModel: UtilityViewModel) {
    var recognizedText by remember { mutableStateOf("") }
    var extractedNumber by remember { mutableStateOf("") }
    var isAutoFillEnabled by remember { mutableStateOf(true) }
    var selectedUtilityType by remember { mutableStateOf(UtilityType.WATER) }
    var noteText by remember { mutableStateOf("") }
    var confidenceLevel by remember { mutableStateOf(0) }
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

    // Функция для обработки распознанного текста
    fun processRecognizedText(text: String, confidence: Int) {
        recognizedText = text
        confidenceLevel = confidence
        if (isAutoFillEnabled && confidence >= 70) { // Только при высокой уверенности
            val number = extractCleanNumber(text)
            if (number.isNotBlank() && number != extractedNumber) {
                extractedNumber = number
                Log.d("AutoFill", "Автоматически заполнено: $number (уверенность: $confidence%)")
            }
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
            if (extractedNumber.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val number = extractedNumber.replace(",", ".").toDoubleOrNull() ?: 0.0
                        viewModel.addReading(
                            type = selectedUtilityType,
                            value = number,
                            note = if (noteText.isNotEmpty()) noteText else "Считано камерой"
                        )
                        navController.popBackStack()
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Сохранить")
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
                // Камера с улучшенным распознаванием текста
                CameraWithEnhancedTextRecognition(
                    onTextRecognized = { text, confidence ->
                        processRecognizedText(text, confidence)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Панель управления и отображения результатов
                RecognitionResultsPanel(
                    recognizedText = recognizedText,
                    extractedNumber = extractedNumber,
                    isAutoFillEnabled = isAutoFillEnabled,
                    confidenceLevel = confidenceLevel,
                    onAutoFillToggle = { isAutoFillEnabled = it },
                    onNumberChange = { extractedNumber = it },
                    selectedUtilityType = selectedUtilityType,
                    onUtilityTypeChange = { selectedUtilityType = it },
                    noteText = noteText,
                    onNoteChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

            } else {
                // Если нет разрешения
                PermissionRequestView(
                    onRequestPermission = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun RecognitionResultsPanel(
    recognizedText: String,
    extractedNumber: String,
    isAutoFillEnabled: Boolean,
    confidenceLevel: Int,
    onAutoFillToggle: (Boolean) -> Unit,
    onNumberChange: (String) -> Unit,
    selectedUtilityType: UtilityType,
    onUtilityTypeChange: (UtilityType) -> Unit,
    noteText: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Переключатель авто-заполнения
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Авто-заполнение",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isAutoFillEnabled,
                onCheckedChange = onAutoFillToggle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор типа счетчика
        Text(
            text = "Тип счетчика:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UtilityType.entries.forEach { type ->
                FilterChip(
                    selected = selectedUtilityType == type,
                    onClick = { onUtilityTypeChange(type) },
                    label = { Text(getUtilityTypeText(type)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Поле для ввода числа
        Text(
            text = "Показания:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = extractedNumber,
            onValueChange = { newValue ->
                // Разрешаем только цифры и запятую
                if (newValue.matches(Regex("[\\d,]*"))) {
                    onNumberChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите показания (только цифры и запятая)") },
            singleLine = true,
            trailingIcon = {
                if (extractedNumber.isNotEmpty()) {
                    IconButton(onClick = { onNumberChange("") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Очистить")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле для заметки
        Text(
            text = "Заметка (необязательно):",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Дополнительная информация") },
            singleLine = true
        )

        // Информация о распознавании
        if (recognizedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        confidenceLevel >= 80 -> Color.Green.copy(alpha = 0.1f)
                        confidenceLevel >= 60 -> Color.Yellow.copy(alpha = 0.1f)
                        else -> Color.Red.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Распознано:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "Уверенность: $confidenceLevel%",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                confidenceLevel >= 80 -> Color.Green
                                confidenceLevel >= 60 -> Color(0xFFFFA000)
                                else -> Color.Red
                            }
                        )
                    }
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestView(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
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
                onClick = onRequestPermission
            ) {
                Text("Запросить разрешение")
            }
        }
    }
}

@Composable
fun CameraWithEnhancedTextRecognition(
    onTextRecognized: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastStableText by remember { mutableStateOf("") }
    var stableCounter by remember { mutableStateOf(0) }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                // Инициализируем камеру с улучшенным распознаванием текста
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(surfaceProvider)
                            }

                        // Настройка анализатора для распознавания текста
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val textAnalyzer = EnhancedDigitalTextRecognitionAnalyzer { text, confidence ->
                            // Стабилизация - текст должен повториться несколько раз
                            if (text != lastStableText) {
                                lastStableText = text
                                stableCounter = 1
                            } else {
                                stableCounter++
                            }

                            // Отправляем только после 3 одинаковых распознаваний подряд
                            if (stableCounter >= 3 && text.isNotBlank()) {
                                onTextRecognized(text, confidence)
                                stableCounter = 0 // Сбрасываем счетчик
                            }
                        }
                        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), textAnalyzer)

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        // Останавливаем предыдущие use cases и запускаем новые
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
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

class EnhancedDigitalTextRecognitionAnalyzer(
    private val onTextRecognized: (String, Int) -> Unit
) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val (cleanText, confidence) = extractDigitalNumbersWithConfidence(visionText)
                    if (cleanText.isNotBlank()) {
                        onTextRecognized(cleanText, confidence)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TextRecognition", "Ошибка распознавания текста: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun extractDigitalNumbersWithConfidence(visionText: Text): Pair<String, Int> {
        var bestText = ""
        var bestConfidence = 0
        var totalConfidence = 0
        var elementCount = 0

        // Анализируем все текстовые элементы
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text
                val lineConfidence = estimateConfidence(line)

                // Фильтруем только цифровые последовательности
                val digitalText = extractCleanDigitalSequence(lineText)
                if (digitalText.isNotBlank() && digitalText.length >= 3) {
                    if (digitalText.length > bestText.length ||
                        (digitalText.length == bestText.length && lineConfidence > bestConfidence)) {
                        bestText = digitalText
                        bestConfidence = lineConfidence
                    }
                }

                totalConfidence += lineConfidence
                elementCount++
            }
        }

        // Если нашли хорошую цифровую последовательность, используем ее
        if (bestText.isNotBlank() && bestConfidence >= 50) {
            return Pair(bestText, bestConfidence)
        }

        // Иначе используем среднюю уверенность по всему тексту
        val averageConfidence = if (elementCount > 0) totalConfidence / elementCount else 0
        val fallbackText = extractCleanDigitalSequence(visionText.text)

        return Pair(fallbackText, averageConfidence)
    }

    private fun estimateConfidence(textElement: Text.Line): Int {
        // Простая эвристика для оценки уверенности
        var confidence = 50 // Базовая уверенность

        // Увеличиваем уверенность для чистых цифровых последовательностей
        val cleanText = textElement.text.replace(Regex("[^\\d,]"), "")
        val digitalRatio = cleanText.length.toDouble() / textElement.text.length.toDouble()

        if (digitalRatio > 0.8) confidence += 30
        if (digitalRatio > 0.9) confidence += 20

        // Уменьшаем уверенность для коротких текстов
        if (textElement.text.length < 3) confidence -= 20

        return confidence.coerceIn(0, 100)
    }
}

// Функция для извлечения чистой цифровой последовательности
private fun extractCleanDigitalSequence(text: String): String {
    if (text.isBlank()) return ""

    // Убираем все символы кроме цифр и запятых
    val cleanText = text.replace(Regex("[^\\d,]"), "")

    // Проверяем что это валидное число (не более одной запятой)
    val commaCount = cleanText.count { it == ',' }
    if (commaCount > 1) {
        return cleanText.replace(",", "") // Убираем лишние запятые
    }

    return cleanText
}

// Функция для извлечения чистого числа (основная)
private fun extractCleanNumber(text: String): String {
    val cleanSequence = extractCleanDigitalSequence(text)

    // Проверяем длину и валидность
    if (cleanSequence.length in 3..8) {
        // Проверяем что это валидное число
        val isValid = cleanSequence.matches(Regex("\\d+[,]?\\d*"))
        if (isValid) {
            return cleanSequence
        }
    }

    return ""
}

private fun getUtilityTypeText(type: UtilityType): String {
    return when (type) {
        UtilityType.WATER -> "Вода"
        UtilityType.ELECTRICITY -> "Электричество"
    }
}