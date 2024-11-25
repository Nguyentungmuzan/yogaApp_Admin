package com.example.universalyogaapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.universalyogaapp.model.Course

@Dao
interface CourseDao {

    @Insert
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    // Delete method by course ID
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: Int)

    // Get all courses
    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<Course>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: Int): Course?
    // Insert a single course and return its generated ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long

    // Insert multiple courses (for bulk insertion)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

}
