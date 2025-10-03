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

    suspend fun addWidget(widget: WidgetData) {
        context.dataStore.edit { prefs ->
            val list = readWidgetsFromPreferences().toMutableList()
            list.add(widget)
            prefs[WIDGETS_KEY] = Json.encodeToString(list)
        }
    }

    suspend fun updateWidget(widget: WidgetData) {
        context.dataStore.edit { prefs ->
            val list = readWidgetsFromPreferences().toMutableList()
            val index = list.indexOfFirst { it.id == widget.id }
            if (index != -1) {
                list[index] = widget
            }
            prefs[WIDGETS_KEY] = Json.encodeToString(list)
        }
    }

    suspend fun deleteWidget(id: Int) {
        context.dataStore.edit { prefs ->
            val list = readWidgetsFromPreferences().toMutableList()
            list.removeAll { it.id == id }
            prefs[WIDGETS_KEY] = Json.encodeToString(list)
        }
    }

    suspend fun getWidget(id: Int): WidgetData? {
        return try {
            val prefs = context.dataStore.data.first()
            val json = prefs[WIDGETS_KEY] ?: "[]"
            val list = Json.decodeFromString<List<WidgetData>>(json)
            list.find { it.id == id }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getWidgetBySystemId(systemId: String): WidgetData? {
        return try {
            val list = readWidgetsFromPreferences()
            list.find { it.systemWidgetId.equals(systemId) }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateWidgetWithSystemId(widgetId: Int, systemWidgetId: Int) {
        context.dataStore.edit { prefs ->
            val list = readWidgetsFromPreferences().toMutableList()
            
            // 更新对应的小部件，添加系统小部件ID
            val index = list.indexOfFirst { it.id == widgetId }
            if (index != -1) {
                list[index] = list[index].copy(systemWidgetId = systemWidgetId.toString())
            }
            
            prefs[WIDGETS_KEY] = Json.encodeToString(list)
        }
    }
}