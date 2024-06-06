package com.example.weather_app.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Launcher: ", isDeviceATablet(this).toString())


        val intentToStart = if (isDeviceATablet(this))
            Intent(this, HomeScreenTabletActivity::class.java)
        else
            Intent(this, HomeScreenActivity::class.java)

        val cityName = intent.getStringExtra("cityName")

        if(cityName != null) {
            Log.d("got city name: ", cityName)
            intentToStart.putExtra("cityName", cityName)
        }

        startActivity(intentToStart)
        finish()
    }

    private fun isDeviceATablet(context: Context): Boolean {
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val widthDp = displayMetrics.widthPixels / resources.displayMetrics.density
        val heightDp = displayMetrics.heightPixels / resources.displayMetrics.density
        Log.d("width: , Height: ", "$widthDp |||| $heightDp")
        val minSizeForTablet = 700

        return widthDp >= minSizeForTablet && heightDp >= minSizeForTablet
    }
}