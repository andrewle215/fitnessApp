package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class MainActivity : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var emailLogin: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_layout)





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userName = findViewById(R.id.name)
        emailLogin = findViewById(R.id.email)
        button = findViewById(R.id.loginButton)

        button.setOnClickListener {
            val name = userName.text.toString()
            val email = emailLogin.text.toString()
            if (validateInput(name, email)) {
                saveUserData(name, email)
                navigateToInfo()
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

    private fun switchToLoginLayout() {
        val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref1: DatabaseReference = firebase.getReference("Name")
        val ref2: DatabaseReference = firebase.getReference("Email")
        ref1.setValue("testing_name_works")
        ref2.setValue("testing_email_works")
    }

    //Main things to add, using the built in health app to get calorie information and total steps that the user did
    //using firebase to create the leaderboard system

