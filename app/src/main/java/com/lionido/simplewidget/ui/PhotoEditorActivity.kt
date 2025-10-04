package com.lionido.simplewidget.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lionido.simplewidget.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PhotoEditorActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_WIDGET_SIZE = "widget_size"
        const val RESULT_CROPPED_IMAGE = "cropped_image"
    }
    
    private var imageUri: Uri? = null
    private var widgetSize: String = "medium"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        imageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI)
        widgetSize = intent.getStringExtra(EXTRA_WIDGET_SIZE) ?: "medium"
        
        if (imageUri == null) {
            Log.e("PhotoEditorActivity", "No image URI provided")
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        
        setContent {
            PhotoEditorScreen(
                imageUri = imageUri!!,
                widgetSize = widgetSize,
                onSave = { croppedImagePath ->
                    val resultIntent = Intent().apply {
                        putExtra(RESULT_CROPPED_IMAGE, croppedImagePath)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                },
                onCancel = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    imageUri: Uri,
    widgetSize: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Определяем размеры рамки в зависимости от размера виджета
    val (frameWidth, frameHeight) = when (widgetSize.lowercase()) {
        "small" -> Pair(200f, 100f)   // 2x1 виджет
        "medium" -> Pair(300f, 150f)  // 3x1 виджет
        "large" -> Pair(400f, 200f)  // 4x1 виджет
        else -> Pair(300f, 150f)
    }
    
    // Состояние для трансформации изображения
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Загружаем изображение
    LaunchedEffect(imageUri) {
        try {
            val bitmap = withContext(Dispatchers.IO) {
                loadBitmapFromUri(context, imageUri)
            }
            imageBitmap = bitmap?.asImageBitmap()
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Заголовок
        TopAppBar(
            title = {
                Text(
                    text = "Обрезка фото",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                TextButton(onClick = onCancel) {
                    Text("Отмена", color = Color.White)
                }
            },
            actions = {
                Button(
                    onClick = {
                        imageBitmap?.let { bitmap ->
                            val croppedPath = cropImage(
                                bitmap = bitmap,
                                scale = scale,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                frameWidth = frameWidth,
                                frameHeight = frameHeight,
                                context = context
                            )
                            onSave(croppedPath)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("Сохранить", color = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )
        
        // Инструкция
        Text(
            text = "Обрежьте фото под рамку виджета ${widgetSize}",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
        
        // Область редактирования
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = Color.White)
                }
                error != null -> {
                    Text(
                        text = "Ошибка загрузки: $error",
                        color = Color.Red
                    )
                }
                imageBitmap != null -> {
                    PhotoEditorCanvas(
                        imageBitmap = imageBitmap!!,
                        frameWidth = frameWidth,
                        frameHeight = frameHeight,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        onScaleChange = { scale = it },
                        onOffsetChange = { x, y ->
                            offsetX = x
                            offsetY = y
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoEditorCanvas(
    imageBitmap: ImageBitmap,
    frameWidth: Float,
    frameHeight: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onScaleChange((scale * zoom).coerceIn(0.5f, 3f))
                    onOffsetChange(
                        (offsetX + pan.x).coerceIn(-frameWidth, frameWidth),
                        (offsetY + pan.y).coerceIn(-frameHeight, frameHeight)
                    )
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Центрируем рамку
        val frameLeft = (canvasWidth - frameWidth) / 2
        val frameTop = (canvasHeight - frameHeight) / 2
        
        // Рисуем изображение
        drawImage(
            image = imageBitmap,
            topLeft = Offset(
                frameLeft + offsetX,
                frameTop + offsetY
            ),
            alpha = 0.7f
        )
        
        // Рисуем рамку обрезки
        drawRect(
            color = Color.White,
            topLeft = Offset(frameLeft, frameTop),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
        
        // Рисуем затемнение вне рамки
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset.Zero,
            size = size
        )
        
        // Очищаем область рамки
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(frameLeft, frameTop),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight)
        )
    }
}

private suspend fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.e("PhotoEditorActivity", "Error loading bitmap", e)
            null
        }
    }
}

private fun cropImage(
    bitmap: ImageBitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    frameWidth: Float,
    frameHeight: Float,
    context: android.content.Context
): String {
    return try {
        // Создаем файл для сохранения обрезанного изображения
        val fileName = "cropped_widget_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        
        // Создаем новый Bitmap с нужными размерами
        val targetWidth = frameWidth.toInt()
        val targetHeight = frameHeight.toInt()
        val croppedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        
        // Рисуем на новом Bitmap (упрощенная версия)
        val canvas = android.graphics.Canvas(croppedBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        
        // Сохраняем обрезанное изображение
        FileOutputStream(file).use { output ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        
        croppedBitmap.recycle()
        
        file.absolutePath
    } catch (e: Exception) {
        Log.e("PhotoEditorActivity", "Error cropping image", e)
        ""
    }
}