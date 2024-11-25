package com.example.universalyogaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.universalyogaapp.database.CourseDatabase
import com.example.universalyogaapp.databinding.ActivityViewCoursesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewCoursesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewCoursesBinding
    private lateinit var adapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = CourseAdapter(
            onViewClick = { course ->
                val intent = Intent(this, ViewClassActivity::class.java).apply {
                    putExtra("COURSE_ID", course.id)
                    putExtra("COURSE_DAY_OF_WEEK", course.day)
                }
                startActivity(intent)
            },
            onEditClick = { course ->
                val intent = Intent(this, EditCourseActivity::class.java).apply {
                    putExtra("COURSE_ID", course.id)
                }
                startActivityForResult(intent, EDIT_COURSE_REQUEST_CODE)
            },
            onDeleteClick = { course ->
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("Are you sure you want to delete the course: ${course.name}?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        deleteCourse(course.id)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                val alert = dialogBuilder.create()
                alert.setTitle("Delete Course")
                alert.show()
            }
        )

        binding.recyclerViewCourses.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCourses.adapter = adapter

        // Button "Reset Data"
        binding.btnResetData.setOnClickListener {
            Log.d("ViewCoursesActivity", "Reset Data button clicked")
            showResetConfirmationDialog()
        }

        Log.d("ViewCoursesActivity", "Reset Data button setup complete")

        // Get database
        loadCourses()
    }

    // Get Courses from database
    private fun loadCourses() {
        lifecycleScope.launch {
            val courseDao = CourseDatabase.getDatabase(this@ViewCoursesActivity).courseDao()
            val courses = courseDao.getAllCourses()
            adapter.submitList(courses)
        }
    }

    private fun resetData() {
        Log.d("ViewCoursesActivity", "resetData called")
        lifecycleScope.launch(Dispatchers.IO) {
            val database = CourseDatabase.getDatabase(this@ViewCoursesActivity)
            database.courseDao().deleteAllCourses()
            database.classInstanceDao().deleteAllClasses()
            withContext(Dispatchers.Main) {
                Log.d("ViewCoursesActivity", "Data deleted, updating UI")
                Toast.makeText(this@ViewCoursesActivity, "All data has been deleted", Toast.LENGTH_SHORT).show()
                loadCourses()
            }
        }
    }

    private fun showResetConfirmationDialog() {
        Log.d("ViewCoursesActivity", "showResetConfirmationDialog called")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete all data? This will delete the course and the class.")
        builder.setPositiveButton("Yes") { _, _ -> resetData() }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // Delete Course
    private fun deleteCourse(courseId: Int) {
        lifecycleScope.launch {
            val courseDao = CourseDatabase.getDatabase(this@ViewCoursesActivity).courseDao()
            courseDao.deleteCourseById(courseId)
            Toast.makeText(this@ViewCoursesActivity, "Course deleted", Toast.LENGTH_SHORT).show()
            loadCourses() // Refresh Course after delete
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_COURSE_REQUEST_CODE && resultCode == RESULT_OK) {
            loadCourses()
        }
    }

    companion object {
        private const val EDIT_COURSE_REQUEST_CODE = 1
    }
}