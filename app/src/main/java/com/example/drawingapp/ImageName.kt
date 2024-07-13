/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 2
 * Date: March 22, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Images")
/**
 * A data class used to represent the file names for images stored in our database
 */
data class ImageName(var fileName: String){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
