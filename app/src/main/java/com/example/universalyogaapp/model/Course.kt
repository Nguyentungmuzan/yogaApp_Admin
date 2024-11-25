// Course.kt
package com.example.universalyogaapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val day: String,
    val time: String,
    val capacity: Int,
    val duration: Int,
    val price: Double,
    val type: String
)
