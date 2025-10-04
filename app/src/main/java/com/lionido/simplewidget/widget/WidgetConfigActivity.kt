package com.lionido.simplewidget.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import coil.compose.AsyncImage
import com.lionido.simplewidget.data.WidgetData
import com.lionido.simplewidget.data.WidgetRepository
import com.lionido.simplewidget.data.WidgetType
import com.lionido.simplewidget.ui.theme.SimpleWidgetTheme
import com.lionido.simplewidget.utils.ImageUtils
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var widgetType: WidgetType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем результат по умолчанию как CANCELED
        setResult(RESULT_CANCELED)

        // Получаем ID виджета из intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Определяем тип виджета из AppWidgetManager
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        widgetType = when {
            info?.provider?.className?.contains("DayCounterWidget") == true -> WidgetType.DAY_COUNTER
            info?.provider?.className?.contains("PhotoWidget") == true -> WidgetType.PHOTO
            else -> null
        }

        // Если ID невалидный, закрываем активность
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            SimpleWidgetTheme {
                WidgetSelectionScreen(
                    widgetId = appWidgetId,
                    widgetType = widgetType,
                    onWidgetSelected = { selectedWidget ->
                        linkWidgetAndFinish(selectedWidget)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private fun linkWidgetAndFinish(widget: WidgetData) {
        val scope = kotlinx.coroutines.MainScope()
        scope.launch {
            val repository = WidgetRepository(this@WidgetConfigActivity)

            // Связываем виджет с системным ID
            repository.updateWidgetWithSystemId(widget.id, appWidgetId)

            // Обновляем виджет
            try {
                val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)

                when (widget.type) {
                    WidgetType.DAY_COUNTER -> DayCounterWidget().update(this@WidgetConfigActivity, glanceId)
                    WidgetType.PHOTO -> PhotoWidget().update(this@WidgetConfigActivity, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Устанавливаем результат OK
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSelectionScreen(
    widgetId: Int,
    widgetType: WidgetType?,
    onWidgetSelected: (WidgetData) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { WidgetRepository(context) }
    val widgets by repository.widgets.collectAsState(initial = emptyList())

    // Фильтруем виджеты по типу
    val filteredWidgets = remember(widgets, widgetType) {
        if (widgetType != null) {
            widgets.filter { it.type == widgetType }
        } else {
            widgets
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выберите виджет") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Отмена")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (filteredWidgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Нет доступных виджетов",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Сначала создайте виджет в приложении",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Выберите виджет для отображения на главном экране:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWidgets) { widget ->
                        WidgetSelectionCard(
                            widget = widget,
                            onClick = { onWidgetSelected(widget) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetSelectionCard(
    widget: WidgetData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Превью виджета
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = if (widget.imageUri != null) Color.Gray else Color(widget.backgroundColor),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                if (widget.imageUri != null) {
                    AsyncImage(
                        model = ImageUtils.getImageUri(context, widget.imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = widget.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (widget.type) {
                        WidgetType.DAY_COUNTER -> "Счетчик дней"
                        WidgetType.PHOTO -> "Фото виджет"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (widget.systemWidgetId != null) {
                    Text(
                        text = "⚠️ Уже используется",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}