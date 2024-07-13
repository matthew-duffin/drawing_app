/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 2
 * Date: March 22, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.drawingapp.databinding.FragmentMenuBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private val listPadding = 4
    private val interfacesPadding = 16
    private val scaledBitmapDimension = 800
    /**
     * Creates the menu UI Screen using Jetpack Compose
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val orientation = resources.configuration.orientation

        // Inflate the layout for this fragmental canvasViewModel : CanvasViewModel by activityViewModels()
        val canvasViewModel: CanvasViewModel by activityViewModels()
        {
            CanvasViewModel.CanvasViewModelFactory((requireActivity().application as DrawingApplication).imageRepository)
        }

        binding = FragmentMenuBinding.inflate(layoutInflater)
        binding.composeView1.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Scaffold {
                    Surface(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LocalStorage()
                                NewImage(canvasViewModel, findNavController())
                                Spacer(Modifier.height(interfacesPadding.dp))

                                val list by canvasViewModel.images.collectAsState(listOf()) // collect data from database
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(listPadding.dp)) {
                                    for (imageName in list!!.asReversed()) { // loop through all file names in database,
                                        // and display info on buttons
                                        item {
                                            ImageDisplay(
                                                imageName,
                                                canvasViewModel,
                                                findNavController()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.Top) {
                                    LocalStorage()
                                    NewImage(canvasViewModel, findNavController())
                                }
                                Spacer(Modifier.height(interfacesPadding.dp))

                                val list by canvasViewModel.images.collectAsState(listOf()) // collect data from database
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(listPadding.dp)) {
                                    for (imageName in list!!.asReversed()) { // loop through all file names in database,
                                        // and display info on buttons
                                        item {
                                            ImageDisplay(
                                                imageName,
                                                canvasViewModel,
                                                findNavController()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return binding.root
        }
    }

    @Composable
            /**
             * Displays the file names and when button is clicked it loads the image into the canvas
             */
    fun ImageDisplay(
        imageName: ImageName,
        vm: CanvasViewModel,
        navController: NavController,
        modifier: Modifier = Modifier
    ) {
        Button(
            shape = RectangleShape,
            onClick = {
                try {
                    vm.load(imageName.fileName)
                    navController.navigate(R.id.selectImage)

                    Log.e("click check", "It worked")
                } catch (e: Exception) {
                    val toast = Toast.makeText(
                        this.context,
                        "Failed to Load",
                        Toast.LENGTH_SHORT
                    ) // in Activity
                    toast.show()
                }
            },
            content =
            {
                Image(

                    bitmap = Bitmap.createScaledBitmap(vm.loadImageMenuIcon(imageName.fileName), scaledBitmapDimension, scaledBitmapDimension, true).asImageBitmap(),
                    contentDescription = imageName.fileName,
                    Modifier.wrapContentSize()
                )
            }
        )
    }

    @Composable
            /**
             * Creates our new image button that is used to create a blank canvas
             */
    fun NewImage(vm: CanvasViewModel, navController: NavController, modifier: Modifier = Modifier) {

        Button(
            onClick = {
                navController.navigate(R.id.selectImage)
                vm.new()
            },

            content =
            {
                Text(
                    text = "New Image",
                    modifier = modifier.padding(interfacesPadding.dp)
                )
            }
        )
    }

    @Preview
    @Composable
            /**
             * Displays the words main menu at top of screen
             */
    fun LocalStorage(modifier: Modifier = Modifier) {
        Text(
            text = "Local Storage",
            fontSize = 30.sp,
            modifier = modifier.padding(interfacesPadding.dp)
        )
    }
}
