package com.example.finalproject

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditData : AppCompatActivity() {

//    private lateinit var caloriesBurnedInput: EditText
    private lateinit var goalCaloriesInput: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_data)

        goalCaloriesInput = findViewById(R.id.goal_calories)
        saveButton = findViewById(R.id.save_button)

        loadExistingData()

        saveButton.setOnClickListener {
            saveData()
        }
    }

    private fun loadExistingData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val caloriesBurned = sharedPref.getFloat("caloriesBurned", 0.0f)
        val goalCalories = sharedPref.getFloat("goalCalories", 0.0f)

//        caloriesBurnedInput.setText(if (caloriesBurned > 0) caloriesBurned.toString() else "")
        goalCaloriesInput.setText(if (goalCalories > 0) goalCalories.toString() else "")
    }

    private fun saveData() {
//        val caloriesBurned = caloriesBurnedInput.text.toString().toFloatOrNull()
        val goalCalories = goalCaloriesInput.text.toString().toFloatOrNull()

//        if (caloriesBurned == null || goalCalories == null) {
        if (goalCalories == null){
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
//            putFloat("caloriesBurned", caloriesBurned)
            putFloat("goalCalories", goalCalories)
            apply()
        }

        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()

        finish()
    }
}
