package com.example.finalproject

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.infopage)

//        val progressBar = findViewById<ProgressBar>(R.id.pb_calories)
//        val caloriesText = findViewById<TextView>(R.id.tv_calories_burned)
//
//        val fitnessData = FitnessData(this)
//
//        fitnessData.fetchCalories()
//
//        progressBar.postDelayed({
//            val calories = fitnessData.totalCalories
//            progressBar.progress = calories.toInt()
//            caloriesText.text = "$calories / 500 kcal"
//        }, 1000) // 1-second delay (adjust if needed)
    }
}
