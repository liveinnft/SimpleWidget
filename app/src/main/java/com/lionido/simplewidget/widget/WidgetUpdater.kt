package com.lionido.simplewidget.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.lionido.simplewidget.data.WidgetData
import com.lionido.simplewidget.data.WidgetType

object WidgetUpdater {

    suspend fun updateWidget(context: Context, widget: WidgetData) {
        widget.systemWidgetId?.let { systemId ->
            try {
                val appWidgetId = systemId.toIntOrNull() ?: return
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

                when (widget.type) {
                    WidgetType.DAY_COUNTER -> {
                        DayCounterWidget().update(context, glanceId)
                    }
                    WidgetType.PHOTO -> {
                        PhotoWidget().update(context, glanceId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateAllWidgets(context: Context, widgets: List<WidgetData>) {
        widgets.forEach { widget ->
            updateWidget(context, widget)
        }
    }
}