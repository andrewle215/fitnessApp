package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class InfoPage : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var caloriesText: TextView
    private lateinit var editDataButton: Button
    private lateinit var leaderboardContainer: LinearLayout

    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("Users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.infopage)

        progressBar = findViewById(R.id.pb_calories)
        caloriesText = findViewById(R.id.tv_calories_burned)
        editDataButton = findViewById(R.id.change_goal)
        leaderboardContainer = findViewById(R.id.leaderboard_container)

        editDataButton.setOnClickListener {
            val intent = Intent(this, EditData::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateGoalData()
        fetchLeaderboardData()
    }

    private fun updateGoalData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val goalCalories = sharedPref.getFloat("goalCalories", 0.0f)

        if (goalCalories > 0) {
            progressBar.max = goalCalories.toInt()
            progressBar.progress = 0
            caloriesText.text = "0 / $goalCalories cal"
        }
    }

    private fun fetchLeaderboardData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserData>()
                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val calories = userSnapshot.child("calories").getValue(Double::class.java) ?: 0.0
                    userList.add(UserData(name, calories))
                }

                userList.sortByDescending { it.calories }

                val topUsers = userList.take(5)

                leaderboardContainer.removeAllViews()

                for ((index, user) in topUsers.withIndex()) {
                    val textView = TextView(this@InfoPage)
                    textView.textSize = 16f
                    textView.text = "${index + 1}. ${user.name} - ${user.calories} cal"
                    textView.layoutParams = LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    )
                    leaderboardContainer.addView(textView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InfoPage", "Failed to retrieve leaderboard data: ${error.message}")
            }
        })
    }

    data class UserData(
        val name: String,
        val calories: Double
    )
}
