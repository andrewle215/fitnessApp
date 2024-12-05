package com.example.finalproject

import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class FitnessData(private val activity: Activity) {

    companion object {
        private const val REQUEST_OAUTH_REQUEST_CODE = 1001
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()


    fun checkPermissionsAndSignIn() {
        val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                activity,
                REQUEST_OAUTH_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            accessFitData()
        }
    }


    fun handlePermissionsResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
            if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                accessFitData()
            } else {
                Log.w("FitnessData", "Permissions not granted by the user.")
            }
        }
    }


    private fun accessFitData() {
        val calendar = Calendar.getInstance()
        val endMillis = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
        Fitness.getHistoryClient(activity, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val totalSteps = response.buckets.flatMap { it.dataSets }.sumOf { dataSet ->
                    dataSet.dataPoints.sumOf { it.getValue(Field.FIELD_STEPS).asInt() }
                }

                val totalCalories = response.buckets.flatMap { it.dataSets }.sumOf { dataSet ->
                    dataSet.dataPoints.sumOf { it.getValue(Field.FIELD_CALORIES).asFloat().toDouble() }
                }

                Log.d("GoogleFit", "Total Steps: $totalSteps")
                Log.d("GoogleFit", "Total Calories: $totalCalories")
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "Failed to read data", e)
            }
    }
}
