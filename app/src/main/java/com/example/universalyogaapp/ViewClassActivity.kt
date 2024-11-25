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
import com.example.universalyogaapp.databinding.ActivityViewClassesBinding
import kotlinx.coroutines.launch

class ViewClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewClassesBinding
    private lateinit var adapter: ClassInstanceAdapter
    private var courseId: Int = -1
    private var courseDayOfWeek: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get courseId and courseDayOfWeek from Intent
        courseId = intent.getIntExtra("COURSE_ID", -1)
        courseDayOfWeek = intent.getStringExtra("COURSE_DAY_OF_WEEK") ?: ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnAddClass.setOnClickListener {
            val intent = Intent(this, AddClassActivity::class.java).apply {
                putExtra("COURSE_ID", courseId)
                putExtra("COURSE_DAY_OF_WEEK", courseDayOfWeek)
            }

            // Check value courseDayOfWeek
            Log.d("ViewClassActivity", "Passing courseDayOfWeek: $courseDayOfWeek")
            startActivity(intent)
        }

        // RecyclerView setup
        adapter = ClassInstanceAdapter(
            showActions = true,  // Show "Edit" and "Delete" button
            onEditClick = { classInstance ->
                val intent = Intent(this, EditClassActivity::class.java).apply {
                    putExtra("CLASS_INSTANCE_ID", classInstance.id)
                    putExtra("COURSE_ID", courseId)
                    putExtra("COURSE_DAY_OF_WEEK", courseDayOfWeek)
                }
                startActivity(intent)
            },
            onDeleteClick = { classInstance ->
                showDeleteConfirmationDialog(classInstance.id)
            }
        )

        binding.recyclerViewClasses.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewClasses.adapter = adapter

        // Get class data from database and display
        loadClassInstances()
    }

    override fun onResume() {
        super.onResume()
        loadClassInstances()
    }

    // Get list of classes from database
    private fun loadClassInstances() {
        lifecycleScope.launch {
            val classDao = CourseDatabase.getDatabase(this@ViewClassActivity).classInstanceDao()
            val classInstances = classDao.getClassInstancesByCourseId(courseId)
            adapter.submitList(classInstances)  // Update data for adapter
        }
    }

    // Show confirmation dialog before deleting
    private fun showDeleteConfirmationDialog(classInstanceId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this class?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteClassInstance(classInstanceId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Delete Course
    private fun deleteClassInstance(classInstanceId: Int) {
        lifecycleScope.launch {
            val classDao = CourseDatabase.getDatabase(this@ViewClassActivity).classInstanceDao()
            classDao.deleteClassInstanceById(classInstanceId)
            Toast.makeText(this@ViewClassActivity, "Class deleted", Toast.LENGTH_SHORT).show()
            loadClassInstances() // Refresh Course after delete
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
