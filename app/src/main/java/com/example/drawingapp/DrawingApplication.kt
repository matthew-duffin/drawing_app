/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 2
 * Date: March 22, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.drawingapp.databinding.FragmentFirebaseBinding
import com.example.drawingapp.databinding.FragmentMenuBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * A drawing application that allows us to access the repository and coroutine scope
 * Has a longer lifetime than activity
 */
class DrawingApplication : Application()
{
    //coroutine scope tied to the application lifetime which we can run suspend functions in
    val scope = CoroutineScope(SupervisorJob())
    //get a reference to the DB singleton
    val db by lazy {ImageDatabase.getDatabase(applicationContext)}

    //create our repository singleton, using lazy to access the DB when we need it
    val imageRepository by lazy { ImageRepository(scope, db.imageDAO()) }
}
