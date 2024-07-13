/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 1
 * Date: Feb 16, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.drawingapp.databinding.FragmentFirebaseBinding
import com.example.drawingapp.databinding.FragmentMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.drawingapp.databinding.FragmentCanvasBinding
import com.skydoves.colorpickerview.listeners.ColorListener
import java.io.ByteArrayOutputStream


/**
 * This fragment is the main UI element of our code. Handles all button listeners and displays all images to the user
 */
class CanvasFragment : Fragment() {
    private lateinit var binding : FragmentCanvasBinding
    private val scalarForPen = 25
    /**
     * This exists to set up all button listeners and connects our view model to the UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Creates the view model using the view model factory
        val canvasViewModel : CanvasViewModel by activityViewModels()
        {
            CanvasViewModel.CanvasViewModelFactory((requireActivity().application as DrawingApplication).imageRepository)
        }
        // inflates the view
        binding = FragmentCanvasBinding.inflate(layoutInflater)
        binding.canvasView.setImageBitmap(canvasViewModel.getBitmap())
        binding.canvasView.setOnTouchListener { _, event ->
            when(event?.action) {
                // when the user first clicks, this allows us to draw and track the coordinates of where they clicked
                MotionEvent.ACTION_DOWN -> {
                    val (scaledX, scaledY) = getScaledCoordinates(event) // scales the coordinates between the bitmap and image view
                    Log.e("Coords", "X: " + scaledX + " Y: " + scaledY)
                    canvasViewModel.draw(scaledX!!, scaledY!!, false) // false needs to be passed in for the path to be properly connected in UI
                    canvasViewModel.draw(scaledX, scaledY, true)
                }
                // When the user drags, allows the path to follow the mouse
                MotionEvent.ACTION_MOVE -> {
                    val (scaledX, scaledY) = getScaledCoordinates(event)
                    Log.e("Coords", "X: " + scaledX + " Y: " + scaledY)
                    canvasViewModel.draw(scaledX!!, scaledY!!, true)
                }
            }
            true // required by setOnTouchListener
        }

        binding.penSize.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scaledWidth = progress.toFloat() * scalarForPen + scalarForPen // scales the width of the pen to correctly draw on bitmap
                Log.e("Unscaled Width", progress.toString())
                Log.e("scaled Width", scaledWidth.toString())
                canvasViewModel.setToolWidth(scaledWidth)
            }
            // added here to fulfill abstract class
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        canvasViewModel.observableCanvas.observe(viewLifecycleOwner) // allows are view model to update to match UI
        {
            binding.canvasView.setImageBitmap(it.getBitmap())
            binding.canvasView.invalidate()
            binding.penSize.progress = ((it.getToolWidth() - scalarForPen)/scalarForPen).toInt()
        }
        binding.circleButton.setOnClickListener()
        {
            canvasViewModel.setToolShape(true) // true indicates the shape is a circle
        }
        binding.squareButton.setOnClickListener()
        {
            canvasViewModel.setToolShape(false) // false indicates the shape is a square
        }

        binding.colorPickerView.setInitialColor(canvasViewModel.getColor())

        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlideBar) // attaches brightness slider to our color picker
        binding.colorPickerView.setColorListener(ColorListener { color, fromUser ->
            canvasViewModel.setColor(color)
        })

        binding.eraserButton.setOnClickListener()
        {
            canvasViewModel.setColor(Color.WHITE) // when erasing, the color should match the canvas which is white
        }
        binding.penButton.setOnClickListener()
        {
            canvasViewModel.setColor(binding.colorPickerView.color) // when the user clicks eraser and then back into the pen tool, it should remember the color recently picked
        }
        binding.saveButton.setOnClickListener()
        {
            canvasViewModel.save()
        }

        binding.loadButton.setOnClickListener()
        {
            findNavController().navigate(R.id.accessImageMenu) // navigate between screens when loading so we can go to main menu``
        }

        binding.invertBitmapButton.setOnClickListener()
        {
            canvasViewModel.invertBitmap()
        }

        binding.saturateButton.setOnClickListener()
        {
            canvasViewModel.saturateBitmap()
        }

        binding.shareFirebaseButton.setOnClickListener()
        {
            canvasViewModel.save()
            findNavController().navigate(R.id.firebaseFragment)
        }

        return binding.root
    }

    /**
     * returns coordinates properly scaled between the image view and bit map
     * Basically converts dp to pixels
     */
    private fun getScaledCoordinates(event: MotionEvent): Pair<Float?, Float?> {
        val x = event.x
        val y = event.y
        val scaledX = x.let {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, it,
                resources.displayMetrics
            )
        }
        val scaledY = y.let {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, it,
                resources.displayMetrics
            )
        }
        return Pair(scaledX, scaledY)
    }
}