package com.lionido.simplewidget.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.lionido.simplewidget.data.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DayCounterWidgetSmall : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayCounterWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        android.util.Log.d("DayCounterWidgetSmall", "Widget deleted, unlinking: ${appWidgetIds.joinToString()}")
        val repository = WidgetRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            appWidgetIds.forEach { widgetId ->
                repository.unlinkSystemWidget(widgetId.toString())
            }
        }
    }
}