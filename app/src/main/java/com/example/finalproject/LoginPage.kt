package com.example.finalproject

import android.content.Context
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class LoginPage : AppCompatActivity() {

    private lateinit var userName : EditText
    private lateinit var button : Button
    private lateinit var emailLogin : EditText

    private fun getName() {

        userName = findViewById(R.id.name)


        button.setOnClickListener {
            val name = userName.text.toString()
            val email = emailLogin.text.toString()

            if (validateInput(name, email)) {
                saveUserData(name, email)
  //              navigateToNextPage() // goes to another page need to work on this
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


}