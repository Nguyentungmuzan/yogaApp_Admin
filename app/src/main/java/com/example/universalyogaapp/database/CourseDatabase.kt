package com.example.universalyogaapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.universalyogaapp.model.ClassInstance
import com.example.universalyogaapp.model.Course

@Database(entities = [Course::class, ClassInstance::class], version = 1)
abstract class CourseDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun classInstanceDao(): ClassInstanceDao

    companion object {
        @Volatile
        private var INSTANCE: CourseDatabase? = null

        fun getDatabase(context: Context): CourseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CourseDatabase::class.java,
                    "course_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
