package com.lionido.simplewidget.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    
    suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val fileName = "widget_image_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
                
                return@withContext file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
    
    fun getImageUri(context: Context, filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                Uri.fromFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun loadAndResizeBitmap(filePath: String, maxWidth: Int = 400, maxHeight: Int = 400): Bitmap? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            
            // Сначала получаем размеры изображения без загрузки в память
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)
            
            // Вычисляем коэффициент масштабирования
            val scaleFactor = calculateInSampleSize(options, maxWidth, maxHeight)
            
            // Загружаем изображение с нужным масштабированием
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
                inJustDecodeBounds = false
            }
            
            val bitmap = BitmapFactory.decodeFile(filePath, loadOptions)
            
            // Дополнительно масштабируем если нужно
            if (bitmap != null && (bitmap.width > maxWidth || bitmap.height > maxHeight)) {
                val scaledBitmap = scaleBitmap(bitmap, maxWidth, maxHeight)
                bitmap.recycle() // Освобождаем память
                scaledBitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
        }
        
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }
}