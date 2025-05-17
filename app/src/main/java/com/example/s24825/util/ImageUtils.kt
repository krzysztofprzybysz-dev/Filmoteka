package com.example.s24825.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Utility class for handling image operations.
 *
 * This class provides functionality for saving images from gallery to the app's
 * internal storage, loading images, and managing image files.
 */
class ImageUtils(private val context: Context) {

    /**
     * Directory name for storing poster images.
     */
    private val POSTERS_DIR = "posters"

    /**
     * Gets the directory for storing poster images.
     */
    private fun getPosterDirectory(): File {
        val dir = File(context.filesDir, POSTERS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Generates a unique filename for a new poster image.
     */
    private fun generateImageFilename(): String {
        return "poster_${UUID.randomUUID()}.jpg"
    }

    /**
     * Saves an image from a Uri to the app's internal storage.
     *
     * @param uri Uri of the image to save (from gallery picker)
     * @return Path to the saved image file, or null if saving failed
     */
    suspend fun saveImageFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // Open an input stream from the Uri
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Create a file in the app's internal storage
                val file = File(getPosterDirectory(), generateImageFilename())

                // Save the image to the file
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                // Return the absolute path to the saved file
                return@withContext file.absolutePath
            }
        } catch (e: IOException) {
            Log.e("ImageUtils", "Error saving image", e)
        }
        return@withContext null
    }

    /**
     * Loads a bitmap from a file path.
     *
     * @param path Path to the image file
     * @return The loaded Bitmap, or null if loading failed
     */
    suspend fun loadImageFromPath(path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            return@withContext BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error loading image", e)
            return@withContext null
        }
    }

    /**
     * Deletes an image file.
     *
     * @param path Path to the image file to delete
     * @return True if deletion was successful, false otherwise
     */
    suspend fun deleteImage(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                return@withContext file.delete()
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error deleting image", e)
        }
        return@withContext false
    }
}