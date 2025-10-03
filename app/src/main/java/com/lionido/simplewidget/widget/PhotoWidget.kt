package com.lionido.simplewidget.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.lionido.simplewidget.MainActivity

class PhotoWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            PhotoWidgetContent()
        }
    }
}

@Composable
fun PhotoWidgetContent() {
    val context = LocalContext.current
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6200EE)))
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "照片小部件",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 18.sp
            )
        )
    }
}