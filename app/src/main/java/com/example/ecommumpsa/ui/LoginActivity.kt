package com.example.ecommumpsa.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommumpsa.data.EcommRepository
import com.example.ecommumpsa.databinding.ActivityLoginBinding
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: EcommRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = EcommRepository(applicationContext)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnLogin.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val result = repository.login(username, password)
                binding.btnLogin.isEnabled = true
                if (result.isSuccess) {
                    val uname = repository.extractUsername(result.getOrNull() ?: "")
                    Toast.makeText(this@LoginActivity, "Welcome $uname", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Autofill if credentials saved
        repository.getSavedUsername()?.let { binding.etUsername.setText(it) }
        repository.getSavedPassword()?.let { binding.etPassword.setText(it) }
    }
}