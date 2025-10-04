package com.lionido.simplewidget.utils

import android.content.Context
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
}