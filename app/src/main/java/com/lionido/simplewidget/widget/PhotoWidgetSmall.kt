package com.lionido.simplewidget.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.lionido.simplewidget.data.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoWidgetSmall : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhotoWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        val repository = WidgetRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            appWidgetIds.forEach { widgetId ->
                repository.unlinkSystemWidget(widgetId.toString())
            }
        }
    }
}