package com.lionido.simplewidget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lionido.simplewidget.data.WidgetData
import com.lionido.simplewidget.data.WidgetRepository
import com.lionido.simplewidget.data.WidgetType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemWidgetDialog(
    systemWidgetId: Int,
    onDismiss: () -> Unit,
    repository: WidgetRepository
) {
    val scope = rememberCoroutineScope()
    val widgets by repository.widgets.collectAsState(initial = emptyList())
    var selectedWidgetId by remember { mutableStateOf<Int?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Выберите виджет для связывания",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "ID системного виджета: $systemWidgetId",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(widgets) { widget ->
                        WidgetItem(
                            widget = widget,
                            isSelected = selectedWidgetId == widget.id,
                            onClick = { selectedWidgetId = widget.id }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedWidgetId?.let { widgetId ->
                                scope.launch {
                                    repository.updateWidgetWithSystemId(widgetId, systemWidgetId)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = selectedWidgetId != null
                    ) {
                        Text("ОК")
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetItem(
    widget: WidgetData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Упрощенное отображение цветового блока
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (widget.imageUri != null) Color.Gray else Color(widget.backgroundColor)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = widget.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (widget.type) {
                        WidgetType.DAY_COUNTER -> "Счетчик дней"
                        WidgetType.PHOTO -> "Фото"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}