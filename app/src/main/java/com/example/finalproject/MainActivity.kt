package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var emailLogin: EditText
    private lateinit var button: Button
    private lateinit var fitnessData: FitnessData
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_layout)

        fitnessData = FitnessData(this)
//        fitnessData.resetCalories {  }
        fitnessData.checkPermissionsAndSignIn {
            Log.d("MainActivity", "Google Fit permissions granted.")
        }



        userName = findViewById(R.id.name)
        emailLogin = findViewById(R.id.email)
        button = findViewById(R.id.loginButton)

        button.setOnClickListener {
            fitnessData.insertCaloriesData {
                Log.d("MainActivity", "Calories inserted successfully.")
                val name = userName.text.toString()
                val email = emailLogin.text.toString()
                if (validateInput(name, email)) {
                    userId = saveUserDataLocal(name, email)
                    saveUserToFirebase(name, email)

                    fitnessData.checkPermissionsAndSignIn {
                        fetchAndSaveCalories()
                    }

                    navigateToInfo()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fitnessData.handlePermissionsResult(requestCode, resultCode, data) {
            Log.d("MainActivity", "Google Fit permissions granted after user interaction.")
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

    private fun saveUserDataLocal(name: String, email: String): String {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val generatedUserId = email.replace(".", "_")
        with(sharedPref.edit()) {
            putString("userName", name)
            putString("userEmail", email)
            putString("userId", generatedUserId)
            apply()
        }
        return generatedUserId
    }

    private fun saveUserToFirebase(name: String, email: String) {
        val generatedUserId = userId ?: return

        val user = mapOf(
            "name" to name,
            "email" to email,
            "calories" to fitnessData.totalCalories
        )

        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(generatedUserId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User saved to Firebase!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Failed to save user to Firebase", it)
                Toast.makeText(this, "Failed to save user to Firebase.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchAndSaveCalories() {
        val generatedUserId = userId ?: return
        fitnessData.fetchCalories(generatedUserId)
    }

    private fun navigateToInfo() {
        val intent = Intent(this, InfoPage::class.java)
        startActivity(intent)
        finish()
    }
}
