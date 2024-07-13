/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 1
 * Date: Feb 16, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
/**
 * This is the back bone of our project
 * Updates the canvas information and the pen tool information
 */
class CanvasModel
{
    private var conf = Bitmap.Config.ARGB_8888 // see other conf types
    private val w = 3000
    private val h = 3000
    private var bmp = Bitmap.createBitmap(w, h, conf)
    private val defaultWidth = 50f
    private var previousX = -1f
    private var previousY = -1f
    private var fileName = ""

    // sets default paint values
    private val textPaint =
        Paint().apply {
            isAntiAlias = true
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeWidth = defaultWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

    private var canvas = Canvas()

    external fun invertBitmap(bitmap: Bitmap)

    external fun saturateBitmap(bitmap: Bitmap, saturationValue: Float)

    // include c++ file
    companion object {
        init {
            System.loadLibrary("drawingapp")
        }
    }

    /**
     * constructor of our canvas model
     */
    init {
      defaultSettings()
    }

    /**
     * Used to reset project to its default settings
     */
    fun defaultSettings()
    {
        bmp.eraseColor(Color.WHITE)
        canvas = Canvas(bmp)
        fileName = ""
    }

    /**
     * this method uses a path object to draw a path on our canvas
     * it does this by keeping track of the previously clicked x and y and drawing a line from their
     * to the next x and y
     */
    @SuppressLint("SuspiciousIndentation")
    fun draw(x: Float, y: Float, drawOrUpdate: Boolean)
    {
        Log.e("passed coords", x.toString() + " " + y.toString())
        val p = Path()
        // checks to make sure the path instantiated
        if(x == -1F || y == -1F)
            p.setLastPoint(x, y)
        else
            p.setLastPoint(previousX, previousY)
        // if drawOrUpdate is true, we call lineTo, otherwise call moveTo.
        // LineTo is called when you click to make sure you can click and add dots
        // moveTo is for when you are dragging your mouse around the canvas
        if(drawOrUpdate)
            p.lineTo(x, y)
        else(!drawOrUpdate)
            p.moveTo(x,y)
        canvas.drawPath(p, textPaint)
        previousX = x
        previousY = y
    }

    /**
     * sets the width of your tool
     */
    fun setToolWidth(width: Float)
    {
        textPaint.strokeWidth = width
    }

    /**
     * a getter for the tool width
     */
    fun getToolWidth() : Float
    {
        return textPaint.strokeWidth
    }

    /**
     * returns the bitMap used in the canvas
     */
    fun getBitmap() : Bitmap
    {
       return bmp
    }

    /**
     * sets the pen shape to a square
     */
    fun setShapeToSquare()
    {
        textPaint.strokeCap = Paint.Cap.SQUARE
    }

    /**
     * sets the pen shape to a circle
     */
    fun setShapeToCircle()
    {
        textPaint.strokeCap = Paint.Cap.ROUND
    }

    /**
     * returns the pen shape
     */
    fun getPenShape() : Paint.Cap
    {
        return textPaint.strokeCap
    }

    /**
     * sets the color of the pen
     */
    fun setColor(color: Int)
    {
        textPaint.color = color
    }

    /**
     * return the color of the pen
     */
    fun getColor() : Int
    {
        return textPaint.color
    }

    /**
     * sets the bitmap to the new bitMap passed in to method
     */
    fun setBitmap(bitMap : Bitmap)
    {
        bmp = bitMap.copy(conf, true)
        canvas.setBitmap(bmp)
    }

    /**
     * Returns the name of the file we are currently editing
     */
    fun getFileName() : String
    {
        return fileName
    }

    /**
     * set the file name whenever we swap files
     */
    fun setFileName(newName : String)
    {
        fileName = newName
    }

    /**
     * Inverts the bitmap
     */
    fun invertBitmap()
    {
        invertBitmap(bmp)
        canvas.setBitmap(bmp)
    }

    /**
     * Saturates the image
     */
    fun saturateBitmap()
    {
        saturateBitmap(bmp, 0.5f)
        canvas.setBitmap(bmp)
    }
}