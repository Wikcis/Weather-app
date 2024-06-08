package com.example.weather_app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weather_app.apiClasses.Refresher

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentToStart = if (Refresher(this, null).isDeviceATablet())
            Intent(this, HomeScreenTabletActivity::class.java)
        else
            Intent(this, HomeScreenActivity::class.java)

        val cityName = intent.getStringExtra("cityName")

        if(cityName != null) {
            intentToStart.putExtra("cityName", cityName)
        }

        startActivity(intentToStart)
        finish()
    }
}