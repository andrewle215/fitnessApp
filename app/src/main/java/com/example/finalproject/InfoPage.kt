package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoPage : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var caloriesText: TextView
    private lateinit var editDataButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.infopage)

        progressBar = findViewById(R.id.pb_calories)
        caloriesText = findViewById(R.id.tv_calories_burned)
        editDataButton = findViewById(R.id.change_goal)

        editDataButton.setOnClickListener {
            val intent = Intent(this, EditData::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateGoalData()
    }

    private fun updateGoalData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val goalCalories = sharedPref.getFloat("goalCalories", 0.0f)

        if (goalCalories > 0) {
            progressBar.max = goalCalories.toInt()
            progressBar.progress = 0
            caloriesText.text = "0 / $goalCalories kcal"
        }
    }
}
