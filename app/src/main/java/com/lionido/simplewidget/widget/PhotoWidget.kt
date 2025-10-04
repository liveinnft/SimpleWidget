package com.lionido.simplewidget.widget

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
import com.lionido.simplewidget.data.WidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository(context)

        val widgetData = withContext(Dispatchers.IO) {
            repository.getWidgetBySystemId(id.toString())
        }

        // Создаем intent для открытия конфигурации
        val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            if (widgetData != null) {
                PhotoWidgetContent(
                    title = widgetData.title,
                    backgroundColor = Color(widgetData.backgroundColor),
                    hasImage = widgetData.imageUri != null,
                    configIntent = configIntent
                )
            } else {
                EmptyWidgetContent(configIntent)
            }
        }
    }
}

@Composable
private fun PhotoWidgetContent(
    title: String,
    backgroundColor: Color,
    hasImage: Boolean,
    configIntent: Intent
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .clickable(actionStartActivity(configIntent))
    ) {
        // Заголовок поверх фона
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
        if (!hasImage) {
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