package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginPage : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var emailLogin: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        userName = findViewById(R.id.name)
        emailLogin = findViewById(R.id.email)
        button = findViewById(R.id.loginButton)

        getName()
    }

    private fun getName() {
        button.setOnClickListener {
            val name = userName.text.toString()
            val email = emailLogin.text.toString()
            if (validateInput(name, email)) {
                saveUserData(name, email)
                navigateToInfo() // Navigate to InfoPage
            }
        }
    }

    private fun validateInput(name: String, email: String): Boolean {
        if (name.isEmpty()) {
            userName.error = "Name is required"
            userName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            emailLogin.error = "Email is required"
            emailLogin.requestFocus()
            return false
        }

        return true
    }

    private fun saveUserData(name: String, email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("userName", name)
            putString("userEmail", email)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    private fun navigateToInfo() {
        val intent = Intent(this, InfoPage::class.java)
        startActivity(intent)
        finish()
    }
}
