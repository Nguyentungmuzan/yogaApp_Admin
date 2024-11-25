package com.example.universalyogaapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.universalyogaapp.database.CourseDatabase
import com.example.universalyogaapp.databinding.ActivityEditClassBinding
import com.example.universalyogaapp.model.ClassInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditClassBinding
    private var classInstanceId: Int = -1
    private var selectedDate: Date? = null
    private var courseId: Int = -1 // Make sure courseId is fetched from intent
    private var courseDayOfWeek: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get classInstanceId and Day of Week from intent
        classInstanceId = intent.getIntExtra("CLASS_INSTANCE_ID", -1)
        courseId = intent.getIntExtra("COURSE_ID", -1)
        courseDayOfWeek = intent.getStringExtra("COURSE_DAY_OF_WEEK") ?: ""

        // Class details
        if (classInstanceId != -1) {
            loadClassInstanceDetails(classInstanceId)
        }

        // (Date Picker) button
        binding.etClassDate.setOnClickListener {
            showDatePickerDialog()
        }

        // "Save" button
        binding.btnSaveClass.setOnClickListener {
            val teacher = binding.etTeacher.text.toString()
            val comments = binding.etComments.text.toString()

            if (selectedDate == null || teacher.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check Day of week
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedDate!!)
            if (dayOfWeek != courseDayOfWeek) {
                binding.tvErrorDayOfWeek.text = "Course day: $courseDayOfWeek\nSchedule day does not match the course day of week. Please select the date again."
                binding.tvErrorDayOfWeek.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                binding.tvErrorDayOfWeek.visibility = View.GONE
            }

            Log.d("EditClassActivity", "Selected day: $dayOfWeek, Course day: $courseDayOfWeek")
            Log.d("EditClassActivity", "Received courseId: $courseId")

            // Update class with correct courseId
            val updatedClassInstance = ClassInstance(
                id = classInstanceId,
                courseId = courseId,
                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate!!),
                teacher = teacher,
                comments = comments
            )

            // Save to database
            lifecycleScope.launch {
                val classInstanceDao = CourseDatabase.getDatabase(this@EditClassActivity).classInstanceDao()
                try {
                    classInstanceDao.updateClassInstance(updatedClassInstance)
                    Toast.makeText(this@EditClassActivity, "Class updated", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("EditClassActivity", "Error updating class instance: ${e.message}")
                    Toast.makeText(this@EditClassActivity, "Error updating class: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadClassInstanceDetails(classInstanceId: Int) {
        lifecycleScope.launch {
            val classInstanceDao = CourseDatabase.getDatabase(this@EditClassActivity).classInstanceDao()
            val classInstance = classInstanceDao.getClassInstanceById(classInstanceId)

            classInstance?.let {
                selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)
                binding.etClassDate.setText(it.date)
                binding.etTeacher.setText(it.teacher)
                binding.etComments.setText(it.comments)
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                binding.etClassDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate!!))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
