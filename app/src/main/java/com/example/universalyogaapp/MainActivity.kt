package com.example.universalyogaapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.universalyogaapp.database.CourseDatabase
import com.example.universalyogaapp.databinding.ActivityMainBinding
import com.example.universalyogaapp.model.ClassInstance
import com.example.universalyogaapp.model.Course
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use ViewBinding to replace findViewById
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // "Add Course" button
        binding.btnAddCourse.setOnClickListener {
            val intent = Intent(this, AddCourseActivity::class.java)
            startActivity(intent)
        }

        // "View Courses" button
        binding.btnViewCourses.setOnClickListener {
            val intent = Intent(this, ViewCoursesActivity::class.java)
            startActivity(intent)
        }

        // "Search" button
        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        // "Upload Data" button
        binding.btnUploadData.setOnClickListener {
            uploadDataToFirebase()
        }

        // Initialize database with default courses if none exist
        addDefaultCourses()
    }

    // Add default courses if no courses exist
    private fun addDefaultCourses() {
        val courseDao = CourseDatabase.getDatabase(this).courseDao()
        val classInstanceDao = CourseDatabase.getDatabase(this).classInstanceDao()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Check and add default courses if they don't exist
                if (courseDao.getAllCourses().isEmpty()) {
                    val kundaliniYoga = Course(
                        name = "Kundalini Yoga",
                        day = "Monday",
                        time = "08:00",
                        capacity = 25,
                        duration = 60,
                        price = 25.0,
                        type = "Focuses on releasing energy through breathwork, meditation, and chanting"
                    )

                    val vinyasaYoga = Course(
                        name = "Vinyasa Yoga",
                        day = "Tuesday",
                        time = "19:00",
                        capacity = 15,
                        duration = 45,
                        price = 12.0,
                        type = "Dynamic flow"
                    )

                    // Insert courses and get their generated IDs
                    val kundaliniId = courseDao.insert(kundaliniYoga)
                    val vinyasaId = courseDao.insert(vinyasaYoga)

                    // Add default classes linked to these course IDs
                    val defaultClasses = listOf(
                        // Kundalini Yoga Classes
                        ClassInstance(
                            courseId = kundaliniId.toInt(),
                            date = "04/11/2024",
                            teacher = "Maya Patel",
                            comments = "Introductory Mediation"
                        ),
                        ClassInstance(
                            courseId = kundaliniId.toInt(),
                            date = "11/11/2024",
                            teacher = "Maya Abbott",
                            comments = "Chakra Alignment Basics"
                        ),
                        // Vinyasa Yoga Classes
                        ClassInstance(
                            courseId = vinyasaId.toInt(),
                            date = "05/11/2024",
                            teacher = "Alex Chen",
                            comments = "Morning Flow Warm-Up"
                        ),
                        ClassInstance(
                            courseId = vinyasaId.toInt(),
                            date = "12/11/2024",
                            teacher = "Leila Anderson",
                            comments = "Sun Salutations"
                        )
                    )

                    classInstanceDao.insertAll(defaultClasses)
                }
            }
        }
    }

    // Check internet connection
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    // Upload database to Firebase
    private fun uploadDataToFirebase() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val courseDao = CourseDatabase.getDatabase(this@MainActivity).courseDao()
            val classInstanceDao = CourseDatabase.getDatabase(this@MainActivity).classInstanceDao()

            val courses = courseDao.getAllCourses()
            val classInstances = classInstanceDao.getAllClassInstances()

            val database = FirebaseDatabase.getInstance()
            val coursesRef = database.getReference("courses")

            try {
                val courseMap = mutableMapOf<String, Any?>()

                courses.forEach { course ->
                    val classesForCourse = classInstances.filter { it.courseId == course.id }
                    val courseData = mapOf(
                        "name" to course.name,
                        "DayofWeek" to course.day,
                        "time" to course.time,
                        "Capacity" to course.capacity,
                        "Duration" to course.duration,
                        "price" to course.price,
                        "description" to course.type,
                        "classInstances" to classesForCourse
                    )
                    courseMap["course_${course.id}"] = courseData
                }

                coursesRef.setValue(courseMap)
                Toast.makeText(this@MainActivity, "Data has been successfully backed up", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Data not successfully backed up", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
