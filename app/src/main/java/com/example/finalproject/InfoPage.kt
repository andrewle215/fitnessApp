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

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.infopage)

        progressBar = findViewById(R.id.pb_calories)
        caloriesText = findViewById(R.id.tv_calories_burned)
        editDataButton = findViewById(R.id.change_goal)
        leaderboardContainer = findViewById(R.id.leaderboard_container)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("userId", null)

        editDataButton.setOnClickListener {
            val intent = Intent(this, EditData::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateProgressBar()
        fetchLeaderboardData()
    }

    private fun updateProgressBar() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val goalCalories = sharedPref.getFloat("goalCalories", 1600f)
        progressBar.max = goalCalories.toInt()

        if (userId == null) {
            Log.e("InfoPage", "User ID is null. Cannot fetch progress.")
            return
        }

        database.child(userId!!).child("calories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val calories = snapshot.getValue(Double::class.java) ?: 0.0
                    progressBar.progress = calories.toInt()
                    caloriesText.text = "${calories.toInt()} / ${goalCalories.toInt()} cal"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoPage", "Failed to fetch calories: ${error.message}")
                }
            })
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
                    val entryContainer = LinearLayout(this@InfoPage).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16, 16, 16, 16)
                        layoutParams = LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(8, 8, 8, 8)
                        }
                        setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                    }

                    val rankAndName = TextView(this@InfoPage).apply {
                        text = "${index + 1}. ${user.name}"
                        textSize = 18f
                        setPadding(8, 4, 8, 4)
                        setTextColor(resources.getColor(android.R.color.holo_blue_dark, theme))
                    }

                    val caloriesText = TextView(this@InfoPage).apply {
                        text = "Calories Burned: ${user.calories.toInt()} cal"
                        textSize = 16f
                        setPadding(8, 4, 8, 4)
                        setTextColor(resources.getColor(android.R.color.black, theme))
                    }

                    entryContainer.addView(rankAndName)
                    entryContainer.addView(caloriesText)

                    leaderboardContainer.addView(entryContainer)
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
