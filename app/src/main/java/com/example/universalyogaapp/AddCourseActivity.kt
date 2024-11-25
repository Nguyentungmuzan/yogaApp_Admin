package com.example.universalyogaapp

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.universalyogaapp.databinding.ActivityAddCourseBinding
import com.example.universalyogaapp.model.Course
import com.example.universalyogaapp.database.CourseDatabase
import kotlinx.coroutines.launch
import java.util.*

class AddCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCourseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Select Day
        setupDayOfWeekSpinner()

        // Back button handling
        binding.btnSubmitCourse.setOnClickListener {
            val dayOfWeek = binding.spinnerDayOfWeek.selectedItem.toString()
            val time = binding.etTime.text.toString()
            val capacity = binding.etCapacity.text.toString().toIntOrNull()
            val duration = binding.etDuration.text.toString().toIntOrNull()
            val price = binding.etPrice.text.toString().toDoubleOrNull()
            val typeOfClass = binding.spinnerTypeOfClass.selectedItem.toString()
            val description = binding.etDescription.text.toString()

            // Check required fields
            var hasError = false

            if (dayOfWeek == "Select Day") {
                binding.errorIconDay.visibility = View.VISIBLE
                hasError = true
            } else {
                binding.errorIconDay.visibility = View.GONE
            }

            if (time.isEmpty()) {
                binding.errorIconTime.visibility = View.VISIBLE
                hasError = true
            } else {
                binding.errorIconTime.visibility = View.GONE
            }

            if (capacity == null) {
                binding.errorIconCapacity.visibility = View.VISIBLE
                hasError = true
            } else {
                binding.errorIconCapacity.visibility = View.GONE
            }

            if (duration == null) {
                binding.errorIconDuration.visibility = View.VISIBLE
                hasError = true
            } else {
                binding.errorIconDuration.visibility = View.GONE
            }

            if (price == null) {
                binding.errorIconPrice.visibility = View.VISIBLE
                hasError = true
            } else {
                binding.errorIconPrice.visibility = View.GONE
            }

            if (hasError) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create Courses
            val newCourse = Course(
                name = typeOfClass,
                day = dayOfWeek,
                time = time,
                capacity = capacity ?: 0,
                duration = duration ?: 0,
                price = price ?: 0.0,
                type = description
            )

            // Save Courses to database
            lifecycleScope.launch {
                val courseDao = CourseDatabase.getDatabase(this@AddCourseActivity).courseDao()
                courseDao.insertCourse(newCourse)
                Toast.makeText(this@AddCourseActivity, "Course added to database", Toast.LENGTH_SHORT).show()

                // Refresh fields after course has been saved
                binding.etDescription.text.clear()
                binding.etTime.text.clear()
                binding.etCapacity.text.clear()
                binding.etDuration.text.clear()
                binding.etPrice.text.clear()
                binding.spinnerDayOfWeek.setSelection(0) // Reset spinner to initial value
                binding.spinnerTypeOfClass.setSelection(0)
            }

            // Show saved course notification
            val courseDetails = """
                Day of Week: $dayOfWeek
                Time: $time
                Capacity: $capacity
                Duration: $duration mins
                Price: £$price
                Type of Class: $typeOfClass
            """.trimIndent()

            Toast.makeText(this, "Course Saved:\n$courseDetails", Toast.LENGTH_LONG).show()
        }

        // Handle when clicking on exclamation mark to show detailed error
        binding.errorIconDay.setOnClickListener {
            Toast.makeText(this, "Please select a day of the week.", Toast.LENGTH_SHORT).show()
        }

        binding.errorIconTime.setOnClickListener {
            Toast.makeText(this, "Please enter a valid time.", Toast.LENGTH_SHORT).show()
        }

        binding.errorIconCapacity.setOnClickListener {
            Toast.makeText(this, "Please enter a valid capacity.", Toast.LENGTH_SHORT).show()
        }

        binding.errorIconDuration.setOnClickListener {
            Toast.makeText(this, "Please enter a valid duration in minutes.", Toast.LENGTH_SHORT).show()
        }

        binding.errorIconPrice.setOnClickListener {
            Toast.makeText(this, "Please enter a valid price.", Toast.LENGTH_SHORT).show()
        }

        // Time Picker Dialog for "Time"
        binding.etTime.setOnClickListener {
            showTimePickerDialog()
        }
    }

    // Function to set the "Day of the Week" list
    private fun setupDayOfWeekSpinner() {
        val daysOfWeek = resources.getStringArray(R.array.days_of_week)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, daysOfWeek)
        binding.spinnerDayOfWeek.adapter = adapter
        binding.spinnerDayOfWeek.setSelection(0)
    }

    // Function to display TimePickerDialog
    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.etTime.setText(time)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
