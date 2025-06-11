package com.example.ecommumpsa.ui.viewmodel

import androidx.lifecycle.*
import com.example.ecommumpsa.data.EcommRepository
import com.example.ecommumpsa.data.model.Attendance
import kotlinx.coroutines.launch

class MainViewModel(private val repo: EcommRepository) : ViewModel() {
    private val _attendance = MutableLiveData<List<Attendance>>()
    val attendance: LiveData<List<Attendance>> = _attendance

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    fun checkIn() {
        _status.value = "Checking in..."
        viewModelScope.launch {
            val result = repo.checkIn()
            _status.value = result.getOrElse { it.message ?: "Error" }
            refreshAttendance()
        }
    }

    fun checkOut() {
        _status.value = "Checking out..."
        viewModelScope.launch {
            val result = repo.checkOut()
            _status.value = result.getOrElse { it.message ?: "Error" }
            refreshAttendance()
        }
    }

    fun refreshAttendance() {
        _status.value = "Refreshing attendance..."
        viewModelScope.launch {
            val result = repo.getAttendance()
            if (result.isSuccess) {
                _attendance.value = result.getOrNull() ?: emptyList()
                _status.value = ""
            } else {
                _status.value = result.exceptionOrNull()?.message ?: "Error"
            }
        }
    }
}