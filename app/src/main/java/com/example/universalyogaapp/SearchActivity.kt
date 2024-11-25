package com.example.universalyogaapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.universalyogaapp.database.CourseDatabase
import com.example.universalyogaapp.databinding.ActivitySearchBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: ClassInstanceAdapter
    private var selectedDate: String? = null
    private var selectedDayOfWeek: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView for Search
        adapter = ClassInstanceAdapter(
            showActions = false  // Do not show "Edit" and "Delete"
        )
        binding.recyclerViewSearchResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSearchResults.adapter = adapter
        setupDayOfWeekSpinner()

        binding.btnPickDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            searchClasses()
        }

        binding.etDayOfWeekOrDate.setOnClickListener {
            binding.btnPickDate.isEnabled = false
            selectedDate = null
        }
    }

    private fun setupDayOfWeekSpinner() {
        val daysOfWeek = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, daysOfWeek)
        binding.etDayOfWeekOrDate.setAdapter(adapter)
        binding.etDayOfWeekOrDate.setOnItemClickListener { _, _, position, _ ->
            selectedDayOfWeek = daysOfWeek[position]
            binding.etDayOfWeekOrDate.setText(selectedDayOfWeek)

            binding.btnPickDate.isEnabled = false
            selectedDate = null
        }
    }

    // Function show DatePickerDialog
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                binding.etDayOfWeekOrDate.setText(selectedDate)
                selectedDayOfWeek = null // Reset Day of Week if select a date
                binding.etDayOfWeekOrDate.isEnabled = false // Disable Day of Week selection after date selection
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Search Function
    @SuppressLint("SetTextI18n")
    private fun searchClasses() {
        val teacherName = binding.etTeacherName.text.toString().trim().takeIf { it.isNotEmpty() }
        val dayOfWeekOrDate = selectedDayOfWeek ?: selectedDate

        if (teacherName == null && dayOfWeekOrDate == null) {
            Toast.makeText(this, "Please enter at least one search criterion.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val classDao = CourseDatabase.getDatabase(this@SearchActivity).classInstanceDao()
            val results = classDao.searchClasses(teacherName, dayOfWeekOrDate)

            if (results.isEmpty()) {
                binding.tvSearchResult.text = "No result search"
                adapter.submitList(emptyList())
            } else {
                binding.tvSearchResult.text = "Search Results"
                adapter.submitList(results)
            }

            // Reset search input fields
            binding.etTeacherName.text.clear()
            binding.etDayOfWeekOrDate.text.clear()
            selectedDate = null
            selectedDayOfWeek = null

            // Re-enable input fields
            binding.btnPickDate.isEnabled = true
            binding.etDayOfWeekOrDate.isEnabled = true
        }
    }

    // Function to hide keyboard
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
