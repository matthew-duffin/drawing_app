/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 2
 * Date: March 22, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * This is a repository that allows our view models to interact with our database
 */
class ImageRepository(
    private val scope: CoroutineScope,
    private val dao: ImageDAO

) {
    var allImages = dao.allImages() // accesses all image file names from the DAO

    //change filepath to context.fileDir or maybe not
    val filepath = Environment.getExternalStorageDirectory().absolutePath +
            "/Download/DrawingAppFiles/"

    /**
     * This loads a file from the external storage and turns into a bitmap that our canvas can use
     */
    fun load(filename: String): Bitmap {
        val dir = File(filepath)
        val file = filepath + filename + ".png"
        if (!dir.exists()) dir.mkdirs()
        // error checking if file exists
        val testfile = File(file)
        if (!testfile.exists()) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        else {
            val bmp = BitmapFactory.decodeFile(testfile.absolutePath)
            return bmp
        }
    }

    /**
     * Used when saving a brand new file to our database. Returns the name of the file
     */
    fun saveAs(bmp: Bitmap): String {
        // file name is a time stamp so we can have unique file names
        val timeStampFileName = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss")
            .withZone(TimeZone.getDefault().toZoneId())
            .format(Instant.now())
        scope.launch {
            saveBitmapToDisk(timeStampFileName, bmp)
            dao.addImageName(ImageName(timeStampFileName)) // add file name to the repo
        }
        return timeStampFileName // returns file name
    }

    /**
     * If the file already exists, we overwrite it on storage, but do not need to change DAO
     * as DAO only stores file names
     */
    fun save(bmp: Bitmap, filename: String) {
        scope.launch {
            saveBitmapToDisk(filename, bmp)
        }
    }

    /**
     * Helper method that handles saving to disk
     */
    fun saveBitmapToDisk(filename: String, bmp: Bitmap) {
        val dir = File(filepath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$filename.png")
        val fOut = FileOutputStream(file)
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
    }
}