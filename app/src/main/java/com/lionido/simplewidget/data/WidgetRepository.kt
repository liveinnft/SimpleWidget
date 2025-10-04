package com.lionido.simplewidget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widgets")

class WidgetRepository(private val context: Context) {
    private val WIDGETS_KEY = stringPreferencesKey("widgets_list")

    val widgets: Flow<List<WidgetData>> = context.dataStore.data.map { prefs ->
        val json = prefs[WIDGETS_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<WidgetData>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun readWidgetsFromPreferences(): List<WidgetData> {
        val prefs = context.dataStore.data.first()
        val current = prefs[WIDGETS_KEY] ?: "[]"
        return try {
            Json.decodeFromString<List<WidgetData>>(current)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun saveWidgets(list: List<WidgetData>) {
        context.dataStore.edit { prefs ->
            prefs[WIDGETS_KEY] = Json.encodeToString(list)
        }
    }

    suspend fun addWidget(widget: WidgetData) {
        val list = readWidgetsFromPreferences().toMutableList()
        list.add(widget)
        saveWidgets(list)
    }

    suspend fun updateWidget(widget: WidgetData) {
        val list = readWidgetsFromPreferences().toMutableList()
        val index = list.indexOfFirst { it.id == widget.id }
        if (index != -1) {
            list[index] = widget
            saveWidgets(list)
        }
    }

    suspend fun deleteWidget(id: Int) {
        val list = readWidgetsFromPreferences().toMutableList()
        list.removeAll { it.id == id }
        saveWidgets(list)
    }

    suspend fun getWidget(id: Int): WidgetData? {
        return readWidgetsFromPreferences().find { it.id == id }
    }

    suspend fun getWidgetBySystemId(systemId: String): WidgetData? {
        android.util.Log.d("WidgetRepository", "Searching for widget with systemId: $systemId")
        val widgets = readWidgetsFromPreferences()
        android.util.Log.d("WidgetRepository", "Total widgets: ${widgets.size}")
        widgets.forEach { widget ->
            android.util.Log.d("WidgetRepository", "Widget ${widget.id}: systemWidgetId=${widget.systemWidgetId}")
        }
        val found = widgets.find { it.systemWidgetId == systemId }
        android.util.Log.d("WidgetRepository", "Found widget: $found")
        return found
    }

    suspend fun updateWidgetWithSystemId(widgetId: Int, systemWidgetId: Int) {
        android.util.Log.d("WidgetRepository", "Updating widget $widgetId with systemId: $systemWidgetId")
        val list = readWidgetsFromPreferences().toMutableList()
        val index = list.indexOfFirst { it.id == widgetId }
        if (index != -1) {
            list[index] = list[index].copy(systemWidgetId = systemWidgetId.toString())
            saveWidgets(list)
            android.util.Log.d("WidgetRepository", "Widget updated successfully")
        } else {
            android.util.Log.e("WidgetRepository", "Widget with id $widgetId not found!")
        }
    }

    // Отвязать виджет от системного ID
    suspend fun unlinkSystemWidget(systemWidgetId: String) {
        android.util.Log.d("WidgetRepository", "Unlinking widget with systemId: $systemWidgetId")
        val list = readWidgetsFromPreferences().toMutableList()
        val index = list.indexOfFirst { it.systemWidgetId == systemWidgetId }
        if (index != -1) {
            list[index] = list[index].copy(systemWidgetId = null)
            saveWidgets(list)
            android.util.Log.d("WidgetRepository", "Widget unlinked successfully")
        } else {
            android.util.Log.e("WidgetRepository", "Widget with systemId $systemWidgetId not found!")
        }
    }
}