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
        return readWidgetsFromPreferences().find { it.systemWidgetId == systemId }
    }

    suspend fun updateWidgetWithSystemId(widgetId: Int, systemWidgetId: Int) {
        val list = readWidgetsFromPreferences().toMutableList()
        val index = list.indexOfFirst { it.id == widgetId }
        if (index != -1) {
            list[index] = list[index].copy(systemWidgetId = systemWidgetId.toString())
            saveWidgets(list)
        }
    }

    // Отвязать виджет от системного ID
    suspend fun unlinkSystemWidget(systemWidgetId: String) {
        val list = readWidgetsFromPreferences().toMutableList()
        val index = list.indexOfFirst { it.systemWidgetId == systemWidgetId }
        if (index != -1) {
            list[index] = list[index].copy(systemWidgetId = null)
            saveWidgets(list)
        }
    }
}