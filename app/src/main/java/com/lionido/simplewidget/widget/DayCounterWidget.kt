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

        val widgetData = withContext(Dispatchers.IO) {
            // Получаем системный ID виджета
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val systemId = appWidgetManager.getAppWidgetId(id)
            repository.getWidgetBySystemId(systemId.toString())
        }

        // Создаем intent для открытия конфигурации
        val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetManager.getAppWidgetId(id))
        }

        provideContent {
            if (widgetData != null && widgetData.startDate != null) {
                val days = calculateDays(widgetData.startDate, widgetData.startFromZero)

                DayCounterContent(
                    title = widgetData.title,
                    days = days,
                    backgroundColor = Color(widgetData.backgroundColor),
                    configIntent = configIntent
                )
            } else {
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