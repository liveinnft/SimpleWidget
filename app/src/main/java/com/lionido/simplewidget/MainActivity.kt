package com.lionido.simplewidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lionido.simplewidget.data.WidgetData
import com.lionido.simplewidget.data.WidgetRepository
import com.lionido.simplewidget.data.WidgetType
import com.lionido.simplewidget.ui.WidgetConfigScreen
import com.lionido.simplewidget.ui.theme.SimpleWidgetTheme
import com.lionido.simplewidget.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleWidgetTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { WidgetRepository(context) }
    val widgets by repository.widgets.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var selectedWidget by remember { mutableStateOf<WidgetData?>(null) }

    when (val widget = selectedWidget) {
        null -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Мои виджеты") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, "Добавить виджет")
                    }
                }
            ) { padding ->
                if (widgets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Нажмите + чтобы добавить виджет",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(widgets) { widget ->
                            WidgetCard(
                                widget = widget,
                                onEdit = { selectedWidget = it },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteWidget(widget.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                AddWidgetDialog(
                    onDismiss = { showDialog = false },
                    onAdd = { type ->
                        scope.launch {
                            val newWidget = WidgetData(
                                id = System.currentTimeMillis().toInt(),
                                type = type,
                                title = if (type == WidgetType.DAY_COUNTER) "Счетчик дней" else "Фото"
                            )
                            repository.addWidget(newWidget)
                            showDialog = false
                            selectedWidget = newWidget
                        }
                    }
                )
            }
        }
        else -> {
            WidgetConfigScreen(
                widgetData = widget,
                onSave = { updatedWidget ->
                    scope.launch {
                        repository.updateWidget(updatedWidget)
                        // Обновляем виджет на главном экране, если он привязан
                        WidgetUpdater.updateWidget(context, updatedWidget)
                        selectedWidget = null
                    }
                },
                onBack = { selectedWidget = null }
            )
        }
    }
}

@Composable
fun WidgetCard(
    widget: WidgetData,
    onEdit: (WidgetData) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onEdit(widget) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Фон
            if (widget.imageUri != null) {
                AsyncImage(
                    model = widget.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(widget.backgroundColor))
                )
            }

            // Контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = widget.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        // Показываем, если виджет привязан к системному виджету
                        if (widget.systemWidgetId != null) {
                            Text(
                                text = "На экране",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (widget.type == WidgetType.DAY_COUNTER && widget.startDate != null) {
                    val days = calculateDays(widget.startDate, widget.startFromZero)
                    Text(
                        text = "$days",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "дней",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AddWidgetDialog(
    onDismiss: () -> Unit,
    onAdd: (WidgetType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите тип виджета") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAdd(WidgetType.DAY_COUNTER) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Счетчик дней")
                }
                Button(
                    onClick = { onAdd(WidgetType.PHOTO) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Виджет с фото")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun calculateDays(startDate: Long, startFromZero: Boolean): Long {
    val start = LocalDate.ofEpochDay(startDate)
    val now = LocalDate.now()
    val days = ChronoUnit.DAYS.between(start, now)
    return if (startFromZero) days else days + 1
}