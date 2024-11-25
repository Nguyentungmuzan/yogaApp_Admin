package com.example.universalyogaapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.universalyogaapp.databinding.ItemCourseBinding
import com.example.universalyogaapp.model.Course

class CourseAdapter(
    private val onViewClick: (Course) -> Unit,
    private val onEditClick: (Course) -> Unit,
    private val onDeleteClick: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(DiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(course: Course) {
            binding.tvCourseName.text = course.name

            binding.tvCourseDetails.text = """
                <b><font color='#000000'>Day:</font></b> <font color='#000000'>${course.day}</font><br>
                <b><font color='#000000'>Time:</font></b> <font color='#000000'>${course.time}</font><br>
                <b><font color='#000000'>Capacity:</font></b> <font color='#000000'>${course.capacity} persons</font><br>
                <b><font color='#000000'>Duration:</font></b> <font color='#000000'>${course.duration} minutes</font><br>
                <b><font color='#000000'>Price:</font></b> <font color='#000000'>£${course.price}</font><br>
                Description: ${course.type}
            """.trimIndent().let { android.text.Html.fromHtml(it) }

            binding.btnViewCourse.setOnClickListener { onViewClick(course) }
            binding.btnEditCourse.setOnClickListener { onEditClick(course) }
            binding.btnDeleteCourse.setOnClickListener { onDeleteClick(course) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = getItem(position)
        holder.bind(course)
    }

    class DiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem
        }
    }
}
