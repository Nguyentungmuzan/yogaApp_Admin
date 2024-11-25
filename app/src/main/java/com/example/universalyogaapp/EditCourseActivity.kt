package com.example.universalyogaapp

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.universalyogaapp.database.CourseDatabase
import com.example.universalyogaapp.databinding.ActivityEditCourseBinding
import com.example.universalyogaapp.model.Course
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCourseBinding
    private var courseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Spinner for "days_of_week" and "type_of_class"
        setupSpinners()

        // Get COURSE_ID from Intent
        courseId = intent.getIntExtra("COURSE_ID", -1)
        if (courseId != -1) {
            loadCourseDetails(courseId)
        }

        // Handling when clicking on TimePicker
        binding.etTime.setOnClickListener {
            showTimePickerDialog()
        }

        // Save button
        binding.btnSaveCourse.setOnClickListener {
            saveCourseDetails()
        }
    }

    // Setup Spinner from strings.xml
    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this,
            R.array.days_of_week,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDayOfWeek.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.type_of_class,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerTypeOfClass.adapter = adapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadCourseDetails(courseId: Int) {
        lifecycleScope.launch {
            val courseDao = CourseDatabase.getDatabase(this@EditCourseActivity).courseDao()
            val course = courseDao.getCourseById(courseId)

            course?.let {
                binding.spinnerDayOfWeek.setSelection(resources.getStringArray(R.array.days_of_week).indexOf(it.day))
                binding.etTime.setText(it.time)
                binding.etCapacity.setText(it.capacity.toString())
                binding.etDuration.setText(it.duration.toString())
                binding.etPrice.setText(it.price.toString())
                binding.spinnerTypeOfClass.setSelection(resources.getStringArray(R.array.type_of_class).indexOf(it.type))
                binding.etDescription.setText(it.type)
            }
        }
    }

    private fun saveCourseDetails() {
        val dayOfWeek = binding.spinnerDayOfWeek.selectedItem.toString()
        val time = binding.etTime.text.toString()
        val capacity = binding.etCapacity.text.toString().toIntOrNull()
        val duration = binding.etDuration.text.toString().toIntOrNull()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val typeOfClass = binding.spinnerTypeOfClass.selectedItem.toString()
        val description = binding.etDescription.text.toString()

        if (capacity == null || duration == null || price == null || time.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedCourse = Course(
            id = courseId,
            name = typeOfClass,
            day = dayOfWeek,
            time = time,
            capacity = capacity,
            duration = duration,
            price = price,
            type = description
        )

        lifecycleScope.launch {
            val courseDao = CourseDatabase.getDatabase(this@EditCourseActivity).courseDao()
            courseDao.updateCourse(updatedCourse)

            // Update the dates of the progressive classes to the new date
            updateClassInstancesDates(courseId, dayOfWeek)

            runOnUiThread {
                Toast.makeText(this@EditCourseActivity, "Course and class dates updated", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun updateClassInstancesDates(courseId: Int, newDayOfWeek: String) {
        val calendarNewDayOfWeek = getCalendarDayOfWeek(newDayOfWeek)

        lifecycleScope.launch {
            val classInstanceDao = CourseDatabase.getDatabase(this@EditCourseActivity).classInstanceDao()
            val classInstances = classInstanceDao.getClassInstancesByCourseId(courseId)

            classInstances.forEach { classInstance ->
                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(classInstance.date)
                val newDate = getNextDateForNewDayOfWeek(currentDate!!, calendarNewDayOfWeek)

                val updatedClassInstance = classInstance.copy(
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(newDate)
                )
                classInstanceDao.updateClassInstance(updatedClassInstance)
            }
        }
    }

    private fun getCalendarDayOfWeek(dayOfWeek: String): Int {
        return when (dayOfWeek) {
            "Sunday" -> Calendar.SUNDAY
            "Monday" -> Calendar.MONDAY
            "Tuesday" -> Calendar.TUESDAY
            "Wednesday" -> Calendar.WEDNESDAY
            "Thursday" -> Calendar.THURSDAY
            "Friday" -> Calendar.FRIDAY
            "Saturday" -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }
    }

    private fun getNextDateForNewDayOfWeek(currentClassDate: Date, newDayOfWeek: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentClassDate

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if (newDayOfWeek >= currentDayOfWeek) {
            calendar.add(Calendar.DAY_OF_MONTH, newDayOfWeek - currentDayOfWeek)
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 7 - (currentDayOfWeek - newDayOfWeek))
        }

        return calendar.time
    }

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
