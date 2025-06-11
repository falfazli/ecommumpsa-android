package com.example.ecommumpsa.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ecommumpsa.data.EcommRepository
import com.example.ecommumpsa.databinding.ActivityMainBinding
import com.example.ecommumpsa.ui.viewmodel.MainViewModel
import com.example.ecommumpsa.ui.viewmodel.MainViewModelFactory
import com.example.ecommumpsa.worker.AutoCheckInWorker
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: EcommRepository
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }
    private val adapter = AttendanceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = EcommRepository(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = repository.getSavedUsername()
        if (username == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.tvWelcome.text = "Welcome, $username"
        binding.rvAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvAttendance.adapter = adapter

        binding.btnCheckIn.setOnClickListener {
            viewModel.checkIn()
        }
        binding.btnCheckOut.setOnClickListener {
            viewModel.checkOut()
        }
        binding.btnAttendance.setOnClickListener {
            viewModel.refreshAttendance()
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        viewModel.attendance.observe(this) {
            adapter.submitList(it)
        }
        viewModel.status.observe(this) {
            if (it.isNotEmpty()) {
                Snackbar.make(binding.coordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            }
        }

        // Fetch attendance on launch
        viewModel.refreshAttendance()

        // Request permissions and schedule auto check-in
        requestPermissionsAndScheduleAutoCheckIn()
    }

    private fun requestPermissionsAndScheduleAutoCheckIn() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val toRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        } else {
            scheduleAutoCheckInWorker()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.values.all { it }) {
                scheduleAutoCheckInWorker()
            } else {
                Snackbar.make(binding.coordinatorLayout, "Auto check-in may not work without permissions.", Snackbar.LENGTH_LONG).show()
            }
        }

    private fun scheduleAutoCheckInWorker() {
        val autoCheckInRequest = PeriodicWorkRequestBuilder<AutoCheckInWorker>(5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AutoCheckIn",
            ExistingPeriodicWorkPolicy.KEEP,
            autoCheckInRequest
        )
    }
}