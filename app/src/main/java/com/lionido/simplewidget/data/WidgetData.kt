package com.lionido.simplewidget.data

import android.graphics.Bitmap
import kotlinx.serialization.Serializable

enum class WidgetType {
    DAY_COUNTER,
    PHOTO
}

@Serializable
data class WidgetData(
    val id: Int,
    val type: WidgetType,
    val title: String,
    val startDate: Long? = null, // для счетчика дней
    val startFromZero: Boolean = true, // отсчитывать с 0 или 1
    val backgroundColor: Long = 0xFF6200EE, // цвет фона
    val imageUri: String? = null, // путь к изображению
    val size: WidgetSize = WidgetSize.MEDIUM,
    val systemWidgetId: String? = null // ID системного виджета
)

enum class WidgetSize {
    SMALL,  // 2x2
    MEDIUM, // 3x2
    LARGE   // 4x2
}