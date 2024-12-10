package com.example.finalproject

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataDeleteRequest
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import java.util.concurrent.TimeUnit
import android.icu.util.Calendar
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.FirebaseDatabase

class FitnessData(private val activity: Activity) {

    companion object {
        private const val REQUEST_OAUTH_REQUEST_CODE = 1001
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
        .build()

    var totalCalories: Double = 0.0
        private set

    fun checkPermissionsAndSignIn(onSuccess: () -> Unit) {
        val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                activity,
                REQUEST_OAUTH_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            Log.d("FitnessData", "Permissions already granted")
            onSuccess()
        }
    }

    fun handlePermissionsResult(requestCode: Int, resultCode: Int, data: Intent?, onSuccess: () -> Unit) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
            if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d("FitnessData", "Permissions granted after user prompt")
                onSuccess()
            } else {
                Log.e("FitnessData", "Permissions not granted by the user")
            }
        }
    }

    private val googleSignInAccount: GoogleSignInAccount
        get() = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)

    fun fetchCalories(userId: String) {
        val calendar = Calendar.getInstance()
        val endMillis = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(activity, googleSignInAccount)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val fetchedCalories = response.buckets.flatMap { it.dataSets }.sumOf { dataSet ->
                    dataSet.dataPoints.sumOf { it.getValue(Field.FIELD_CALORIES).asFloat().toDouble() }
                }

                Log.d("FitnessData", "Fetched Calories: $fetchedCalories")

                // Save fetched calories to Firebase or update in-app
                totalCalories = fetchedCalories
                val database = FirebaseDatabase.getInstance().getReference("Users")
                database.child(userId).updateChildren(mapOf("calories" to totalCalories))
                    .addOnSuccessListener {
                        Log.d("FitnessData", "Calories successfully updated in Firebase for user: $userId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FitnessData", "Failed to update calories in Firebase", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FitnessData", "Failed to fetch calorie data from Google Fit", e)
            }
    }

    fun insertCaloriesData(onComplete: () -> Unit) {
        val dataSource = DataSource.Builder()
            .setAppPackageName(activity.packageName)
            .setDataType(DataType.TYPE_CALORIES_EXPENDED)
            .setType(DataSource.TYPE_RAW)
            .build()

        val dataPoint = DataPoint.create(dataSource)
            .setTimeInterval(
                System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
                System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
        dataPoint.getValue(Field.FIELD_CALORIES).setFloat(200f)

        val dataSet = DataSet.create(dataSource)
        dataSet.add(dataPoint)

        Fitness.getHistoryClient(activity, googleSignInAccount)
            .insertData(dataSet)
            .addOnSuccessListener {
                Log.d("FitnessData", "Data successfully inserted into Google Fit.")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FitnessData", "Failed to insert data into Google Fit", e)
            }
    }

    fun resetCalories(onComplete: () -> Unit) {
        val calendar = Calendar.getInstance()
        val endMillis = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        val request = DataDeleteRequest.Builder()
            .setTimeInterval(startMillis, endMillis, TimeUnit.MILLISECONDS)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED)
            .build()

        Fitness.getHistoryClient(activity, GoogleSignIn.getAccountForExtension(activity, fitnessOptions))
            .deleteData(request)
            .addOnSuccessListener {
                Log.d("FitnessData", "Successfully deleted old calorie data.")
                totalCalories = 0.0 // Reset in-app variable
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FitnessData", "Failed to delete old calorie data", e)
            }
    }
}
