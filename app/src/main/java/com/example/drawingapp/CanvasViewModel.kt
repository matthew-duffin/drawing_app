/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 1
 * Date: Feb 16, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * The canvas view model connects the UI in the fragment to our Model
 * Allows us to use MVVM architecture
 */
class CanvasViewModel(private val repository: ImageRepository) : ViewModel(){
    private var canvasModel = CanvasModel()
    private val canvasLiveData = MutableLiveData(canvasModel)
    val observableCanvas = canvasLiveData as LiveData<out CanvasModel>
    private val circleSelected = MutableLiveData(true)
    var usedCanvas = MutableLiveData(false)
    var downloadedImages = pullImagesFromFirebase()

    var images = repository.allImages

    /**
     * A wrapper method for our canvas model draw method
     * Makes sure the live data is updated.
     */
    fun draw(x: Float, y: Float, drawOrUpdate: Boolean){
        canvasLiveData.value!!.draw(x, y, drawOrUpdate)
        canvasLiveData.value = canvasLiveData.value
    }

    /**
     * updates the tool width in our canvas model
     */
    fun setToolWidth(width: Float)
    {
        canvasModel.setToolWidth(width)
    }

    /**
     * returns the bit map from the canvas model
     */
    fun getBitmap() : Bitmap
    {
        return canvasModel.getBitmap()
    }

    /**
     * sets the shape of the tool in the canvas model.
     * True means circle, false means square
     */
    fun setToolShape(circleOrSquare: Boolean)
    {
        circleSelected.value = circleOrSquare
        if(circleSelected.value == true)
            canvasModel.setShapeToCircle()
        else
            canvasModel.setShapeToSquare()
    }

    /**
     * inform the canvas model to change the color.
     */
    fun setColor(color: Int)
    {
        canvasModel.setColor(color)
    }

    /**
     * Returns the color currently selected
     */
    fun getColor() : Int
    {
        return canvasModel.getColor()
    }

    /**
     * Calls canvas model save functions based on if we are doing
     * saveAs or a save.
     */
    fun save()
    {
        val bmp = canvasModel.getBitmap()
        if(canvasModel.getFileName() == "") { // if no file name, means this is a new file so saveAs
            val newFileName = repository.saveAs(bmp)
            canvasModel.setFileName(newFileName)
        }
        else
            repository.save(bmp,canvasModel.getFileName())
    }

    fun setBitmap(bmp: Bitmap)
    {
        canvasModel.setBitmap(bmp)
    }

    /**
     * retrieve image file names from database and load them into the canvas
     */
    fun load(filename : String)
    {
        canvasModel.setBitmap(repository.load(filename))
        canvasModel.setFileName(filename)
        canvasLiveData.value = canvasLiveData.value // invalidate to update view
    }

    /**
     * this is used to load all the images as icons when in main menu
     */
    fun loadImageMenuIcon(filename: String) : Bitmap
    {
        return repository.load(filename)
    }

    /**
     * used to reset canvas when a new project is selected
     */
    fun new()
    {
        canvasModel.defaultSettings()
    }

    /**
     * Inverts the image colors
     */
    fun invertBitmap()
    {
        canvasModel.invertBitmap()
    }

    /**
     * Saturates the image so the highest color value becomes brighter
     */
    fun saturateBitmap()
    {
        canvasModel.saturateBitmap()
    }

    /**
     * Returns the name of the current file
     */
    fun getFileName(): String {
        return canvasModel.getFileName()
    }

    /**
     * Allows us to download images from our firebase repo
     */
    fun pullImagesFromFirebase(): Flow<List<Bitmap>> {
        return flow {
            val ref = FirebaseStorage.getInstance().reference
            val testFileRef = ref.child("userImages/")

            try {
                // Use withContext to switch to IO dispatcher for network call
                val result = withContext(Dispatchers.IO) {
                    testFileRef.listAll().await()
                }
                val images = mutableListOf<Bitmap>()
                for (item in result.items) {
                    try {
                        val bytes = withContext(Dispatchers.IO) {
                            item.getBytes(Long.MAX_VALUE).await()  // Be cautious with Long.MAX_VALUE in production!
                        }
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        bitmap?.let {
                            images.add(it)
                        }
                    } catch (e: IOException) {
                        // Handle the case where an image fails to load
                        Log.e("Download Error", "Error downloading an image: ${e.message}")
                    }
                }
                // Emit the list of items (StorageReference)
                emit(images)

            } catch (e: Exception) {
                // Handle the error case appropriately
                Log.e("Error", "Failed to fetch images: ${e.message}")
                emit(emptyList()) // Emit an empty list or rethrow the exception based on your error handling strategy
            }
        }
    }
    /**
     * A view model factory used to create our view model in the fragment
     */
    class CanvasViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CanvasViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CanvasViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
