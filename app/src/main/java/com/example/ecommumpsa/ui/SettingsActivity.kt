package com.example.ecommumpsa.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommumpsa.data.EcommRepository
import com.example.ecommumpsa.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var repository: EcommRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = EcommRepository(applicationContext)

        binding.btnLogout.setOnClickListener {
            repository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}