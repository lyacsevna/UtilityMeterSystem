package com.example.utilitymetersystem.presentation.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.utilitymetersystem.data.models.UtilityType
import com.example.utilitymetersystem.presentation.viewmodels.UtilityViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
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
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Создаем временный файл для фотографии
    val photoFile = remember { createImageFile(context) }
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }

    // Ланчер для галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            currentImageUri = it
            showImagePreview = true
            processImageFromUri(context, it) { text, confidence ->
                recognizedText = text
                confidenceLevel = confidence
                if (isAutoFillEnabled && confidence >= 70) {
                    val number = extractCleanNumber(text)
                    if (number.isNotBlank()) {
                        extractedNumber = number
                    }
                }
            }
        }
    }

    // Ланчер для съемки фото
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentImageUri = photoUri
            showImagePreview = true
            processImageFromUri(context, photoUri) { text, confidence ->
                recognizedText = text
                confidenceLevel = confidence
                if (isAutoFillEnabled && confidence >= 70) {
                    val number = extractCleanNumber(text)
                    if (number.isNotBlank()) {
                        extractedNumber = number
                    }
                }
            }
        }
    }

    // Функция для сохранения и перехода на главную
    fun saveAndNavigate() {
        val number = extractedNumber.replace(",", ".").toDoubleOrNull() ?: 0.0
        viewModel.addReading(
            type = selectedUtilityType,
            value = number,
            note = if (noteText.isNotEmpty()) noteText else "Считано${if (currentImageUri != null) " с фото" else " камерой"}"
        )
        // Переход на главный экран
        navController.navigate("main") {
            popUpTo("main") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Сканирование показаний") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка для выбора из галереи
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Галерея")
                    }
                    // Кнопка для создания фотографии
                    IconButton(
                        onClick = {
                            takePictureLauncher.launch(photoUri)
                        }
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Сфотографировать")
                    }
                }
            )
        },
        floatingActionButton = {
            if (extractedNumber.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { saveAndNavigate() }
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
            if (showImagePreview && currentImageUri != null) {
                // Показываем превью выбранного изображения
                ImagePreviewSection(
                    imageUri = currentImageUri!!,
                    onBackToCamera = {
                        showImagePreview = false
                        currentImageUri = null
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Режим живой камеры
                CameraWithEnhancedTextRecognition(
                    onTextRecognized = { text, confidence ->
                        recognizedText = text
                        confidenceLevel = confidence
                        if (isAutoFillEnabled && confidence >= 70) {
                            val number = extractCleanNumber(text)
                            if (number.isNotBlank() && number != extractedNumber) {
                                extractedNumber = number
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Панель управления (всегда видна)
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
                hasImage = currentImageUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun ImagePreviewSection(
    imageUri: Uri,
    onBackToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Превью изображения с реальной картинкой
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Используем Coil для загрузки и отображения изображения
            AsyncImage(
                model = imageUri,
                contentDescription = "Выбранное изображение",
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )

            // Полупрозрачный оверлей с информацией
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = "Изображение",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Изображение загружено",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Обрабатывается для распознавания текста...",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Кнопка возврата к камере
        Button(
            onClick = onBackToCamera,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Icon(Icons.Default.Camera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Вернуться к живой камере")
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
    hasImage: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Статус режима
        if (hasImage) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, tint = Color.Blue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Режим обработки фото",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Blue
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        // Поле для ввода числа
        Text(
            text = "Показания:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = extractedNumber,
            onValueChange = { newValue ->
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
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(12.dp))
            RecognitionInfoCard(recognizedText, confidenceLevel)
        }
    }
}

@Composable
fun RecognitionInfoCard(recognizedText: String, confidenceLevel: Int) {
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

@Composable
fun CameraWithEnhancedTextRecognition(
    onTextRecognized: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // В функции CameraWithEnhancedTextRecognition замените:
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

private fun extractDigitalNumbersWithConfidence(visionText: Text): Pair<String, Int> {
    var bestText = ""
    var bestConfidence = 0
    var totalConfidence = 0
    var elementCount = 0

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            val lineText = line.text
            val lineConfidence = estimateConfidence(line)

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

    if (bestText.isNotBlank() && bestConfidence >= 50) {
        return Pair(bestText, bestConfidence)
    }

    val averageConfidence = if (elementCount > 0) totalConfidence / elementCount else 0
    val fallbackText = extractCleanDigitalSequence(visionText.text)

    return Pair(fallbackText, averageConfidence)
}

// Вспомогательная функция тоже должна быть вне класса
private fun estimateConfidence(textElement: Text.Line): Int {
    var confidence = 50

    val cleanText = textElement.text.replace(Regex("[^\\d,]"), "")
    val digitalRatio = cleanText.length.toDouble() / textElement.text.length.toDouble()

    if (digitalRatio > 0.8) confidence += 30
    if (digitalRatio > 0.9) confidence += 20

    if (textElement.text.length < 3) confidence -= 20

    return confidence.coerceIn(0, 100)
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
                    // ИСПРАВЛЕНИЕ: убрать деструктуризацию
                    val result = extractDigitalNumbersWithConfidence(visionText)
                    val cleanText = result.first
                    val confidence = result.second
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

// Функции для работы с изображениями
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

private fun processImageFromUri(
    context: Context,
    uri: Uri,
    onResult: (String, Int) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val (cleanText, confidence) = extractDigitalNumbersWithConfidence(visionText)
                onResult(cleanText, confidence)
            }
            .addOnFailureListener { e ->
                Log.e("ImageProcessing", "Ошибка обработки изображения: ${e.message}")
                onResult("", 0)
            }
    } catch (e: Exception) {
        Log.e("ImageProcessing", "Ошибка загрузки изображения: ${e.message}")
        onResult("", 0)
    }
}