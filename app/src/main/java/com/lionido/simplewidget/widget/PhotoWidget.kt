package com.lionido.simplewidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.io.File
import androidx.glance.Image
import androidx.glance.ImageProvider
import com.lionido.simplewidget.utils.ImageUtils
import com.lionido.simplewidget.data.WidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository(context)
        
        // Получаем системный ID виджета из GlanceAppWidgetManager
        val glanceAppWidgetManager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
        val systemId = glanceAppWidgetManager.getAppWidgetId(id)
        android.util.Log.d("PhotoWidget", "GlanceId: $id, SystemId: $systemId")
        
        // Попробуем также использовать сам id как системный ID
        val alternativeSystemId = id.toString()
        android.util.Log.d("PhotoWidget", "Alternative SystemId: $alternativeSystemId")

        val widgetData = withContext(Dispatchers.IO) {
            android.util.Log.d("PhotoWidget", "Looking for widget with systemId: $systemId")
            var widget = repository.getWidgetBySystemId(systemId.toString())
            android.util.Log.d("PhotoWidget", "Found widget with systemId: $widget")
            
            // Если не найден, попробуем альтернативный ID
            if (widget == null) {
                android.util.Log.d("PhotoWidget", "Trying alternative systemId: $alternativeSystemId")
                widget = repository.getWidgetBySystemId(alternativeSystemId)
                android.util.Log.d("PhotoWidget", "Found widget with alternative systemId: $widget")
            }
            
            widget
        }

        // Создаем intent для открытия конфигурации
        val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, systemId)
        }

        provideContent {
            android.util.Log.d("PhotoWidget", "WidgetData: $widgetData")
            if (widgetData != null) {
                android.util.Log.d("PhotoWidget", "Title: ${widgetData.title}, ImageUri: ${widgetData.imageUri}")
                PhotoWidgetContent(
                    title = widgetData.title,
                    backgroundColor = Color(widgetData.backgroundColor),
                    imageUri = widgetData.imageUri,
                    configIntent = configIntent
                )
            } else {
                android.util.Log.d("PhotoWidget", "WidgetData is null, showing configuration prompt")
                ConfigurationPromptContent(configIntent)
            }
        }
    }
}

@Composable
private fun PhotoWidgetContent(
    title: String,
    backgroundColor: Color,
    imageUri: String?,
    configIntent: Intent,
    widgetSize: String = "medium"
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .clickable(actionStartActivity(configIntent))
    ) {
        // Если есть изображение, показываем его
        if (imageUri != null) {
            val file = File(imageUri)
            if (file.exists()) {
                // Используем сжатую версию изображения для виджета с учетом размера
                val bitmap = ImageUtils.loadAndResizeBitmap(file.absolutePath, widgetSize = widgetSize)
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                } else {
                    // Если не удалось загрузить изображение, показываем серый фон
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(ColorProvider(Color.Gray.copy(alpha = 0.3f))),
                        content = {}
                    )
                }
            }
        }

        // Заголовок поверх фона/изображения
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Если нет изображения, показываем подсказку
        if (imageUri == null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Добавьте фото",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ConfigurationPromptContent(configIntent: Intent) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6200EE)))
            .clickable(actionStartActivity(configIntent))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Нажмите для настройки",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Фото виджет",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun EmptyWidgetContent(configIntent: Intent) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Gray))
            .clickable(actionStartActivity(configIntent))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Настройте виджет",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 14.sp
            )
        )
    }
}