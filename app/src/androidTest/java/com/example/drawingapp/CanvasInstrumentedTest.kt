package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.graphics.toColor
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestScope

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.File

import com.example.drawingapp.FirebaseFragment
import kotlinx.coroutines.Dispatchers

/**
 * Instrumented test, which will execute on an Android device.
 * Basically tests all methods of canvas model as this is the backbone of the whole project.
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CanvasInstrumentedTest {

    private val testCanvasModel = CanvasModel()
    private lateinit var db: ImageDatabase
    private lateinit var dao: ImageDAO
    private lateinit var repository: ImageRepository
    private val fragment = FirebaseFragment()

    @Before
    fun setup() {
        Firebase.auth.useEmulator("10.0.2.2", 9099)
        Firebase.storage.useEmulator("10.0.2.2", 9199)
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ImageDatabase::class.java
        ).build()

        dao = db.imageDAO()
        repository = ImageRepository(TestScope(), dao)
    }

    /**
     * Tests downloading an image from the firebase
     */
    @Test
    fun testShare() {
        var email = "testemail"
        var password = "testpassword"
        Firebase.auth.createUserWithEmailAndPassword(email, password)

        val bmp = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        fragment.shareImage(bmp, "test.png")

        var downloadedBitmap : Bitmap
        val fileRef = Firebase.storage.reference.child("userImages/test.png")
        fileRef.getBytes(3000*3000)
            .addOnSuccessListener { bytes ->
                downloadedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val testbmp = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
                testbmp.eraseColor(Color.WHITE)
                for (x in 0 until testbmp.width) {
                    for (y in 0 until testbmp.height) {
                        assertEquals(downloadedBitmap.getPixel(x, y), testbmp.getPixel(x, y))
                    }
                }
        }
            .addOnFailureListener()
            {
                assertFalse(true)
            }
        // checks that every pixel matches

    }

    /**
     * Tests the C++ invert method by inverting a white canvas bitmap
     */
    @Test
    fun testInvert() {
        val bmp = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.BLACK)
        testCanvasModel.invertBitmap()
        val testCanvasBmp = testCanvasModel.getBitmap()

        // checks that every pixel matches
        for (x in 0 until bmp.width) {
            for (y in 0 until bmp.height) {
                assertEquals(bmp.getPixel(x, y), testCanvasBmp.getPixel(x, y))
            }
        }
    }

    /**
     * Tests the C++ saturate method by saturating a canvas with a primary Red canvas
     */
    @Test
    fun testSaturate() {
        val bmp = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.argb(255, 150, 1, 1))
        testCanvasModel.setBitmap(bmp)
        bmp.eraseColor(Color.RED)

        testCanvasModel.saturateBitmap()
        val testCanvasBmp = testCanvasModel.getBitmap()

        // checks that every pixel matches
        for (x in 0 until bmp.width) {
            for (y in 0 until bmp.height) {
                assertEquals(bmp.getPixel(x, y), testCanvasBmp.getPixel(x, y))
            }
        }
    }

    /**
     * Tests the canvas model for changing pen size
     */
    @Test
    fun testCanvasModelPenSize() {
        assertEquals(50f, testCanvasModel.getToolWidth())
        testCanvasModel.setToolWidth(100f)
        assertEquals(100f, testCanvasModel.getToolWidth())
    }

    /**
     * Tests the canvas model for changing the pen shape
     */
    @Test
    fun testCanvasModelPenShape() {

        assertTrue(Paint.Cap.ROUND == testCanvasModel.getPenShape())
        testCanvasModel.setShapeToSquare()
        assertTrue(Paint.Cap.SQUARE == testCanvasModel.getPenShape())
        testCanvasModel.setShapeToCircle()
        assertTrue(Paint.Cap.ROUND == testCanvasModel.getPenShape())
    }

    /**
     * Tests the canvas model for changing the pen color
     */
    @Test
    fun testCanvasModelPenColor() {
        testCanvasModel.setColor(Color.RED)
        assertTrue(Color.RED == testCanvasModel.getColor())
    }

    /**
     * This test checks that the bitmap returned from canvasModel is correct.
     */
    @Test
    fun testGetBitMap() {
        val bmp = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        val canvasBmp = testCanvasModel.getBitmap()

        assertEquals(bmp.height, canvasBmp.height)
        assertEquals(bmp.width, canvasBmp.width)

        // checks that every pixel matches
        for (x in 0 until bmp.width) {
            for (y in 0 until bmp.height) {
                assertEquals(bmp.getPixel(x, y), canvasBmp.getPixel(x, y))
            }
        }
    }

    /**
     * This tests our draw method in our canvas Model
     */
    @Test
    fun testDraw() {
        // tests that the canvas is its default color
        var canvasBmp = testCanvasModel.getBitmap()
        assertEquals(Color.WHITE, canvasBmp.getPixel(0, 0).toColor().toArgb())

        testCanvasModel.setColor(Color.RED)
        // makes sure the pathing is right
        testCanvasModel.draw(0f, 0f, false)
        testCanvasModel.draw(0f, 0f, true)

        canvasBmp = testCanvasModel.getBitmap()
        assertEquals(Color.RED, canvasBmp.getPixel(0, 0).toColor().toArgb())
    }

    /**
     *  Testing the save bitmap to disk method
     */
    @Test
    fun testSaveBitmapToDisk() {
        val bmp = Bitmap.createBitmap(3000, 3000, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        val filepath = Environment.getExternalStorageDirectory().absolutePath +
                "/Download/DrawingAppFiles/"
        val file = filepath + "testing4.png"
        val testfile = File(file)
        testfile.delete()
        repository.saveBitmapToDisk("testing4", bmp)

        Log.e("filepath", filepath)
        Log.e("file", file)
        assertTrue(testfile.exists())

        testfile.delete()
    }
}