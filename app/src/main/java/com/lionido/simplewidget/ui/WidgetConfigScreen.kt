package com.lionido.simplewidget.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.lionido.simplewidget.data.WidgetData
import com.lionido.simplewidget.data.WidgetSize
import com.lionido.simplewidget.data.WidgetType
import com.lionido.simplewidget.utils.ImageUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    widgetData: WidgetData,
    onSave: (WidgetData) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(widgetData.title) }
    var selectedDate by remember {
        mutableStateOf(
            widgetData.startDate?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        )
    }
    var startFromZero by remember { mutableStateOf(widgetData.startFromZero) }
    var backgroundColor by remember { mutableStateOf(Color(widgetData.backgroundColor)) }
    var imageUri by remember { mutableStateOf(widgetData.imageUri) }
    var useImage by remember { mutableStateOf(widgetData.imageUri != null) }
    // Размер виджета больше не выбирается пользователем
    var showDatePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val photoEditorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val croppedImagePath = result.data?.getStringExtra(PhotoEditorActivity.RESULT_CROPPED_IMAGE)
            croppedImagePath?.let {
                imageUri = it
                useImage = true
            }
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Открываем фоторедактор
            val intent = android.content.Intent(context, PhotoEditorActivity::class.java).apply {
                putExtra(PhotoEditorActivity.EXTRA_IMAGE_URI, it)
                putExtra(PhotoEditorActivity.EXTRA_WIDGET_SIZE, "medium") // По умолчанию средний размер
            }
            photoEditorLauncher.launch(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройка виджета") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val updated = widgetData.copy(
                                title = title,
                                startDate = if (widgetData.type == WidgetType.DAY_COUNTER)
                                    selectedDate.toEpochDay() else null,
                                startFromZero = startFromZero,
                                backgroundColor = backgroundColor.value.toLong(),
                                imageUri = if (useImage) imageUri else null
                            )
                            onSave(updated)
                        }
                    ) {
                        Icon(Icons.Default.Check, "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )

            // Для счетчика дней
            if (widgetData.type == WidgetType.DAY_COUNTER) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Дата начала",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Начинать с нуля")
                            Switch(
                                checked = startFromZero,
                                onCheckedChange = { startFromZero = it }
                            )
                        }
                    }
                }
            }


            // Фон
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Фон",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !useImage,
                            onClick = { useImage = false },
                            label = { Text("Цвет") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = useImage,
                            onClick = { useImage = true },
                            label = { Text("Фото") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!useImage) {
                        Button(
                            onClick = { showColorPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = backgroundColor
                            )
                        ) {
                            Text("Выбрать цвет", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Выбрать фото")
                        }

                        imageUri?.let { uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                AsyncImage(
                                    model = ImageUtils.getImageUri(context, uri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // Превью
            Text(
                "Предпросмотр",
                style = MaterialTheme.typography.titleMedium
            )

            WidgetPreview(
                title = title,
                backgroundColor = backgroundColor,
                imageUri = if (useImage) imageUri?.let { ImageUtils.getImageUri(context, it) } else null,
                showDays = widgetData.type == WidgetType.DAY_COUNTER,
                days = if (widgetData.type == WidgetType.DAY_COUNTER) {
                    calculateDaysPreview(selectedDate, startFromZero)
                } else 0
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000
            )
            
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                }
            }
            
            DatePicker(
                state = datePickerState
            )
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = backgroundColor,
            onColorSelected = {
                backgroundColor = it
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
fun WidgetPreview(
    title: String,
    backgroundColor: Color,
    imageUri: Uri?,
    showDays: Boolean,
    days: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (showDays) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$days",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "дней",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Выберите цвет",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Простой выбор цветов
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val colors = listOf(
                        Color.Red,
                        Color.Blue,
                        Color.Green,
                        Color.Yellow,
                        Color.Magenta,
                        Color.Cyan
                    )
                    
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .clickable { onColorSelected(color) }
                                .padding(4.dp)
                        ) {
                            if (color == currentColor) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

fun calculateDaysPreview(selectedDate: LocalDate, startFromZero: Boolean): Long {
    val now = LocalDate.now()
    val days = ChronoUnit.DAYS.between(selectedDate, now)
    return if (startFromZero) days else days + 1
}