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
import com.example.universalyogaapp.databinding.ActivityAddClassBinding
import com.example.universalyogaapp.model.ClassInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddClassBinding
    private var courseId: Int = -1
    private var selectedDate: Date? = null
    private var courseDayOfWeek: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get courseId and Day of Week from intent
        courseId = intent.getIntExtra("COURSE_ID", -1)
        courseDayOfWeek = intent.getStringExtra("COURSE_DAY_OF_WEEK") ?: ""

        // Check log when receiving intent
        Log.d("AddClassActivity", "Received courseDayOfWeek: $courseDayOfWeek")

        // Handling date picker button (Date Picker)
        binding.etClassDate.setOnClickListener {
            showDatePickerDialog()
        }

        // "Save" Button
        binding.btnSaveClass.setOnClickListener {
            val teacher = binding.etTeacher.text.toString()
            val comments = binding.etComments.text.toString()

            // Check required fields
            if (selectedDate == null || teacher.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check the date matches the day of the week of the course
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedDate)

            // Compare days of the week
            if (dayOfWeek != courseDayOfWeek) {
                binding.tvErrorDayOfWeek.text = "Course day: $courseDayOfWeek\nSchedule day does not match the course day of week. Please select the date again"
                binding.tvErrorDayOfWeek.visibility = View.VISIBLE
                Log.d("AddClassActivity", "Day mismatch: Selected day: $dayOfWeek, Expected course day: $courseDayOfWeek")
                return@setOnClickListener
            } else {
                binding.tvErrorDayOfWeek.visibility = View.GONE
            }
            Log.d("AddClassActivity", "Selected day: $dayOfWeek, Course day: $courseDayOfWeek")

            // Create new Class
            val classInstance = ClassInstance(
                courseId = courseId,
                date = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(selectedDate!!),
                teacher = teacher,
                comments = comments
            )

            // Save Class to database
            lifecycleScope.launch {
                val classInstanceDao = CourseDatabase.getDatabase(this@AddClassActivity).classInstanceDao()
                classInstanceDao.insertClassInstance(classInstance)
                Toast.makeText(this@AddClassActivity, "The class has been added", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Function to display DatePickerDialog
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                binding.etClassDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(selectedDate!!))
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
