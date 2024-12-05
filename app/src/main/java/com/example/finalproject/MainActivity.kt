package com.example.finalproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        switchToLoginLayout()

    }

    private fun switchToLoginLayout() {
        setContentView(R.layout.login_layout)
        val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref1: DatabaseReference = firebase.getReference("Name")
        val ref2: DatabaseReference = firebase.getReference("Email")
        ref1.setValue("testing_name_works")
        ref2.setValue("testing_email_works")
    }

    //Main things to add, using the built in health app to get calorie information and total steps that the user did
    //using firebase to create the leaderboard system

}