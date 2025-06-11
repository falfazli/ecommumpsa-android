package com.example.ecommumpsa.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommumpsa.data.model.Attendance
import com.example.ecommumpsa.databinding.ItemAttendanceBinding

class AttendanceAdapter : ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(AttendanceDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttendanceViewHolder(private val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Attendance) {
            binding.tvTime.text = "Time: ${item.time}"
            binding.tvIp.text = "IP: ${item.ip}"
            binding.tvLocation.text = "Location: ${item.location}"
        }
    }

    object AttendanceDiff : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance) =
            oldItem.time == newItem.time && oldItem.ip == newItem.ip

        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance) = oldItem == newItem
    }
}