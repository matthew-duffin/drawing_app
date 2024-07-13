/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 2
 * Date: March 22, 2024
 * Overview: This is a simple drawing application that allows the user to draw pictures, erase, and adjust pen color, size, and shape
 */
package com.example.drawingapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities = [ImageName::class], version = 1, exportSchema = false)
/**
 * This represents a database that stores the file names of our images to be used in saving and loading
 */
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDAO(): ImageDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ImageDatabase? = null

        /**
         * used to get database singleton
         */
        fun getDatabase(context: Context): ImageDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                //if another thread initialized this before we got the lock
                //return the object they created
                if (INSTANCE != null) return INSTANCE!!
                //otherwise we're the first thread here, so create the DB
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageDatabase::class.java,
                    "image_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

@Dao
/**
 * A DAO that allows us to add image file names and get the list of image file names from DB
 */
interface ImageDAO {

    //marked as suspend so the thread can yield in case the DB update is slow
    @Insert
    suspend fun addImageName(data: ImageName)

    @Query("SELECT * from images ORDER BY fileName DESC")
    fun allImages(): Flow<List<ImageName>>

}