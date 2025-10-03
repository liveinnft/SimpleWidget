package com.lionido.simplewidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import coil.compose.AsyncImage
import com.lionido.simplewidget.MainActivity
import com.lionido.simplewidget.R
import com.lionido.simplewidget.data.WidgetData
import com.lionido.simplewidget.data.WidgetRepository
import com.lionido.simplewidget.data.WidgetType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import android.content.Intent

class DayCounterWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository(context)
        // 获取widget ID
        val widgetId = id.toString().hashCode()
        
        // Get widget data outside of composable scope
        val widget = repository.getWidget(widgetId)
        
        provideContent {
            if (widget != null && widget.type == WidgetType.DAY_COUNTER) {
                DayCounterWidgetContent(widget)
            } else {
                // 如果找不到widget，显示默认内容
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF6200EE)))
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击配置",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DayCounterWidgetContent(widget: WidgetData) {
    val context = LocalContext.current
    val size = LocalSize.current
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                if (widget.imageUri != null) {
                    ColorProvider(Color.Black)
                } else {
                    ColorProvider(Color(widget.backgroundColor))
                }
            )
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        // 显示背景图片（如果有）
        if (widget.imageUri != null) {
            // 注意：Glance中不能直接使用coil的AsyncImage，这里仅作示意
            // 实际实现中需要使用Glance的Image组件加载图片
        }
        
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = widget.title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            
            if (widget.startDate != null) {
                val days = calculateDaysFromString(widget.startDate, widget.startFromZero)
                Text(
                    text = days.toString(),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "дней",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

private fun calculateDaysFromString(startDate: String, startFromZero: Boolean): Long {
    return try {
        val start = LocalDate.parse(startDate)
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(start, today)
        if (startFromZero) days else days + 1
    } catch (e: Exception) {
        0L
    }
}

