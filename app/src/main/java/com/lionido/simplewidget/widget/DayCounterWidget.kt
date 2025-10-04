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
import com.lionido.simplewidget.data.WidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DayCounterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository(context)
        
        // Получаем системный ID виджета из GlanceAppWidgetManager
        val glanceAppWidgetManager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
        val systemId = glanceAppWidgetManager.getAppWidgetId(id)
        android.util.Log.d("DayCounterWidget", "GlanceId: $id, SystemId: $systemId")
        
        // Попробуем также использовать сам id как системный ID
        val alternativeSystemId = id.toString()
        android.util.Log.d("DayCounterWidget", "Alternative SystemId: $alternativeSystemId")

        val widgetData = withContext(Dispatchers.IO) {
            android.util.Log.d("DayCounterWidget", "Looking for widget with systemId: $systemId")
            var widget = repository.getWidgetBySystemId(systemId.toString())
            android.util.Log.d("DayCounterWidget", "Found widget with systemId: $widget")
            
            // Если не найден, попробуем альтернативный ID
            if (widget == null) {
                android.util.Log.d("DayCounterWidget", "Trying alternative systemId: $alternativeSystemId")
                widget = repository.getWidgetBySystemId(alternativeSystemId)
                android.util.Log.d("DayCounterWidget", "Found widget with alternative systemId: $widget")
            }
            
            widget
        }

        // Создаем intent для открытия конфигурации
        val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, systemId)
        }

        provideContent {
            android.util.Log.d("DayCounterWidget", "WidgetData: $widgetData")
            if (widgetData != null) {
                android.util.Log.d("DayCounterWidget", "StartDate: ${widgetData.startDate}")
                if (widgetData.startDate != null) {
                    val days = calculateDays(widgetData.startDate, widgetData.startFromZero)
                    android.util.Log.d("DayCounterWidget", "Calculated days: $days")

                    DayCounterContent(
                        title = widgetData.title,
                        days = days,
                        backgroundColor = Color(widgetData.backgroundColor),
                        configIntent = configIntent
                    )
                } else {
                    android.util.Log.d("DayCounterWidget", "StartDate is null, showing empty content")
                    EmptyWidgetContent(configIntent)
                }
            } else {
                android.util.Log.d("DayCounterWidget", "WidgetData is null, showing empty content")
                EmptyWidgetContent(configIntent)
            }
        }
    }

    private fun calculateDays(startDate: Long, startFromZero: Boolean): Long {
        val start = LocalDate.ofEpochDay(startDate)
        val now = LocalDate.now()
        val days = ChronoUnit.DAYS.between(start, now)
        return if (startFromZero) days else days + 1
    }
}

@Composable
private fun DayCounterContent(
    title: String,
    days: Long,
    backgroundColor: Color,
    configIntent: Intent
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .clickable(actionStartActivity(configIntent))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "$days",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "дней",
                style = TextStyle(
                    color = ColorProvider(Color.White),
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