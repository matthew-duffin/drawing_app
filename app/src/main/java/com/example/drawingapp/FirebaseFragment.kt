package com.example.drawingapp
/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 3
 * Date: April 19, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.drawingapp.databinding.FragmentFirebaseBinding
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

/**
 * This is a simple fragment to handle most firebase functionality
 */
class FirebaseFragment : Fragment() {
    private lateinit var binding: FragmentFirebaseBinding
    private val listPadding = 4
    private val scaledBitmapDimension = 800

    /**
     * We decided to lock the firebase views to not show horizontal and stay locked vertical. This was a decision made due to time constraints
     */
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // or any other desired orientation
    }

    /**
     * Allows rotation in other methods
     */
    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // reset to allow orientation changes
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val canvasViewModel: CanvasViewModel by activityViewModels()
        {
            CanvasViewModel.CanvasViewModelFactory((requireActivity().application as DrawingApplication).imageRepository)
        }

        binding = FragmentFirebaseBinding.inflate(layoutInflater)
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
                        // set current user
                        var user by remember { mutableStateOf(Firebase.auth.currentUser) }

                        // Handle login compose stuff
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // if there is no user, go through logging in to fire base
                            if (user == null) {
                                Column {
                                    //UI for inputting username and password
                                    var email by remember { mutableStateOf("") }
                                    var password by remember { mutableStateOf("") }
                                    Text("Not logged in")
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email") })
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Password") },
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                    Row {
                                        Button(onClick = {
                                            if (email != "" || password != "") {

                                                Firebase.auth.signInWithEmailAndPassword(
                                                    email,
                                                    password
                                                )
                                                    .addOnCompleteListener(requireActivity()) { task ->
                                                        if (task.isSuccessful) {
                                                            user = Firebase.auth.currentUser
                                                        } else {
                                                            email = "login failed, try again"
                                                        }
                                                    }
                                            }
                                        }) {
                                            Text("Log In")
                                        }
                                        Button(onClick = {
                                            if (email != "" || password != "") {

                                                Firebase.auth.createUserWithEmailAndPassword(
                                                    email,
                                                    password
                                                )
                                                    .addOnCompleteListener(requireActivity()) { task ->
                                                        if (task.isSuccessful) {
                                                            user = Firebase.auth.currentUser
                                                        } else {
                                                            email =
                                                                "Create user failed, try again"
                                                            Log.e(
                                                                "Create user error",
                                                                "${task.exception}"
                                                            )
                                                        }
                                                    }
                                            }
                                        }) {
                                            Text("Sign Up")
                                        }
                                    }
                                }
                            } else { // if user signs in, navigate to canvas if button clicked
                                Text("Welcome ${user!!.email}")
                                Column(verticalArrangement = Arrangement.spacedBy(listPadding.dp)) {
                                    Button(
                                        onClick = {
                                            canvasViewModel.usedCanvas.value = true
                                            findNavController().navigate(R.id.CanvasFragment)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth() // Match parent width
                                            .height(height = ButtonDefaults.MinHeight)
                                    )
                                    {
                                        Text("Start Drawing")
                                    }
                                    Button(
                                        onClick = {
                                            user = null
                                            Firebase.auth.signOut()
                                        }, modifier = Modifier
                                            .fillMaxWidth() // Match parent width
                                            .height(height = ButtonDefaults.MinHeight)
                                    )
                                    {
                                        Text("Log out")
                                    }

                                    // show the share button once view model has been initialized
                                    if (canvasViewModel.usedCanvas.value == true) {
                                        // upload data
                                        Button(
                                            onClick = {
                                                shareImage(canvasViewModel.getBitmap(), canvasViewModel.getFileName())
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth() // Match parent width
                                                .height(height = ButtonDefaults.MinHeight)
                                        )
                                        {
                                            Text("Share")
                                        }
                                    }
                                }
                                val imageList by canvasViewModel.downloadedImages.collectAsState(
                                    listOf()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(
                                        listPadding.dp
                                    )

                                ) {
                                    for (image in imageList) { // loop through all file names in database, this is used to display to user for download
                                        item {
                                            // download data
                                            Button(
                                                shape = RectangleShape,
                                                content =
                                                {
                                                    Image(
                                                        bitmap = Bitmap.createScaledBitmap(
                                                            image,
                                                            scaledBitmapDimension,
                                                            scaledBitmapDimension,
                                                            true
                                                        ).asImageBitmap(),
                                                        contentDescription = "Downloaded Image",
                                                        Modifier.wrapContentSize()
                                                    )
                                                },
                                                onClick = {
                                                    canvasViewModel.usedCanvas.value = true
                                                    canvasViewModel.setBitmap(image)
                                                    findNavController().navigate(R.id.CanvasFragment)
                                                }

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

    /**
     * Helper method for sharing images to the firebase storage
     */
    fun shareImage(canvasBitmap : Bitmap, filename : String){
        val byteArrayOutputStream = ByteArrayOutputStream()
        val currentBitmap = canvasBitmap
        //save it into PNG format (in memory, not a file)
        currentBitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            byteArrayOutputStream
        )
        val data =
            byteArrayOutputStream.toByteArray() //bytes of the PNG

        //upload it to firestore object storage
        val ref = Firebase.storage.reference
        val fileRef =
            ref.child("userImages/${filename}.png")
        var uploadTask = fileRef.putBytes(data)
        uploadTask
            .addOnFailureListener { e ->
                Log.e(
                    "PICUPLOAD",
                    "Failed !$e"
                )
            }
            .addOnSuccessListener {
                Log.e(
                    "PICUPLOAD",
                    "Success!"
                )
            }

    }
}
