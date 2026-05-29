package com.litroenade.yunjiweather.data.local.prefs

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object CustomThemeImageStore {
    private const val THEME_IMAGE_DIR = "themes/custom"

    @Throws(IOException::class)
    fun importImage(context: Context, sourceUri: Uri): String {
        val appContext = context.applicationContext
        val directory = customThemeDirectory(appContext)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("无法创建自定义主题图片目录")
        }
        val destination = File(directory, "custom_theme_${System.currentTimeMillis()}.image")
        appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("无法读取选择的图片")
        return Uri.fromFile(destination).toString()
    }

    fun pruneImportedImages(context: Context, keepImageUri: String) {
        if (keepImageUri.isBlank()) {
            return
        }
        val keepFile = runCatching {
            val uri = Uri.parse(keepImageUri)
            if (uri.scheme != "file") {
                return
            }
            File(requireNotNull(uri.path))
        }.getOrNull() ?: return
        pruneImportedImages(customThemeDirectory(context.applicationContext), keepFile)
    }

    fun pruneImportedImages(context: Context, keepImageUris: Collection<String>) {
        val keepFiles = keepImageUris.mapNotNull { imageUri ->
            runCatching {
                val uri = Uri.parse(imageUri)
                if (uri.scheme != "file") {
                    return@mapNotNull null
                }
                File(requireNotNull(uri.path)).canonicalFile
            }.getOrNull()
        }.toSet()
        if (keepFiles.isEmpty()) {
            return
        }
        val directory = customThemeDirectory(context.applicationContext)
        directory.listFiles()?.forEach { file ->
            val canonicalFile = runCatching { file.canonicalFile }.getOrNull()
            if (file.isFile && canonicalFile != null && !keepFiles.contains(canonicalFile) && isInsideDirectory(file, directory)) {
                file.delete()
            }
        }
    }

    fun deleteImportedImage(context: Context, imageUri: String) {
        if (imageUri.isBlank()) {
            return
        }
        val file = runCatching {
            val uri = Uri.parse(imageUri)
            if (uri.scheme != "file") {
                return
            }
            File(requireNotNull(uri.path))
        }.getOrNull() ?: return
        val directory = customThemeDirectory(context.applicationContext)
        if (isInsideDirectory(file, directory)) {
            file.delete()
        }
    }

    fun deleteAllImportedImages(context: Context) {
        val directory = customThemeDirectory(context.applicationContext)
        if (!directory.exists() || !directory.isDirectory) {
            return
        }
        directory.listFiles()?.forEach { file ->
            if (file.isFile && isInsideDirectory(file, directory)) {
                file.delete()
            }
        }
    }

    fun deleteCacheImage(context: Context, imageUri: String) {
        val file = runCatching {
            val uri = Uri.parse(imageUri)
            if (uri.scheme != "file") {
                return
            }
            File(requireNotNull(uri.path))
        }.getOrNull() ?: return
        val cacheDirectory = context.applicationContext.cacheDir
        if (isInsideDirectory(file, cacheDirectory)) {
            file.delete()
        }
    }

    private fun customThemeDirectory(context: Context): File {
        return File(context.filesDir, THEME_IMAGE_DIR)
    }

    private fun pruneImportedImages(directory: File, keepFile: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile && file != keepFile && isInsideDirectory(file, directory)) {
                file.delete()
            }
        }
    }

    private fun isInsideDirectory(file: File, directory: File): Boolean {
        val canonicalFile = runCatching { file.canonicalFile }.getOrNull() ?: return false
        val canonicalDirectory = runCatching { directory.canonicalFile }.getOrNull() ?: return false
        return canonicalFile.path.startsWith(canonicalDirectory.path + File.separator)
    }
}
