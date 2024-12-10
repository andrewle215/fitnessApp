package com.example.finalproject

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.widget.SeekBar
import android.widget.TextView



class EditData : AppCompatActivity() {

    private lateinit var goalCaloriesInput: EditText
    private lateinit var seekBar: SeekBar
    private lateinit var seekBarValue: TextView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_data)

        goalCaloriesInput = findViewById(R.id.goal_calories)
        seekBar = findViewById(R.id.seekbar_calories)
        seekBarValue = findViewById(R.id.seekbar_value)
        saveButton = findViewById(R.id.save_button)

        loadExistingData()

        // Update EditText and TextView based on SeekBar value
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                goalCaloriesInput.setText(progress.toString())
                seekBarValue.text = "SeekBar Value: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            saveData()
        }
    }

    private fun loadExistingData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val goalCalories = sharedPref.getFloat("goalCalories", 0.0f)

        if (goalCalories > 0) {
            goalCaloriesInput.setText(goalCalories.toString())
            seekBar.progress = goalCalories.toInt()
            seekBarValue.text = "SeekBar Value: ${goalCalories.toInt()}"
        }
    }

    private fun saveData() {
        val goalCalories = goalCaloriesInput.text.toString().toFloatOrNull()

        if (goalCalories == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("goalCalories", goalCalories)
            apply()
        }

        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
