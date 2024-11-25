package com.example.universalyogaapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.universalyogaapp.databinding.ItemClassInstanceBinding
import com.example.universalyogaapp.model.ClassInstance

class ClassInstanceAdapter(
    private val showActions: Boolean,  // Show "Edit" and "Delete" buttons
    private val onEditClick: ((ClassInstance) -> Unit)? = null,
    private val onDeleteClick: ((ClassInstance) -> Unit)? = null
) : ListAdapter<ClassInstance, ClassInstanceAdapter.ClassInstanceViewHolder>(DiffCallback()) {

    class ClassInstanceViewHolder(val binding: ItemClassInstanceBinding) : RecyclerView.ViewHolder(binding.root) {
        // Function to bind data to ViewHolder
        @SuppressLint("SetTextI18n")
        fun bind(
            classInstance: ClassInstance,
            showActions: Boolean,
            onEditClick: ((ClassInstance) -> Unit)?,
            onDeleteClick: ((ClassInstance) -> Unit)?
        ) {
            binding.apply {
                // Add header before each data field
                tvClassDate.text = "Date: ${classInstance.date}"
                tvClassTeacher.text = "Teacher Name: ${classInstance.teacher}"
                tvClassComments.text = "Comments: ${classInstance.comments}"

                if (showActions) {
                    btnEdit.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE

                    btnEdit.setOnClickListener { onEditClick?.invoke(classInstance) }
                    btnDelete.setOnClickListener { onDeleteClick?.invoke(classInstance) }
                } else {
                    // Hide buttons if not needed
                    btnEdit.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassInstanceViewHolder {
        val binding = ItemClassInstanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassInstanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClassInstanceViewHolder, position: Int) {
        val classInstance = getItem(position)
        holder.bind(classInstance, showActions, onEditClick, onDeleteClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<ClassInstance>() {
        override fun areItemsTheSame(oldItem: ClassInstance, newItem: ClassInstance): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClassInstance, newItem: ClassInstance): Boolean {
            return oldItem == newItem
        }
    }
}
