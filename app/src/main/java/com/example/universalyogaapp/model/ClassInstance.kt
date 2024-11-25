package com.example.universalyogaapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_instances",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ClassInstance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,      // The course ID associated with this class
    val date: String,       // Date of class (string format "dd/MM/yyyy")
    val teacher: String,    // Name Teacher
    val comments: String? = null  // Comment (Optional)
)
