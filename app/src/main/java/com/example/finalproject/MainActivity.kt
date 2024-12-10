package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
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
                }
            }
        }


        val adView: AdView = AdView(this)
        val adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)

        val adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId

        val builder: AdRequest.Builder = AdRequest.Builder()
        builder.addKeyword("workout").addKeyword("fitness")
        val request: AdRequest = builder.build()

        val adLayout: LinearLayout = findViewById(R.id.ad_view)
        adLayout.addView(adView)

        adView.loadAd(request)


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
        fitnessData.fetchCalories(generatedUserId) {
            navigateToInfo()
        }
    }


    private fun navigateToInfo() {
        val intent = Intent(this, InfoPage::class.java)
        startActivity(intent)
        finish()
    }
}
