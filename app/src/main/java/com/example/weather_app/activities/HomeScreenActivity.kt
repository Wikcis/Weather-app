package com.example.weather_app.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather_app.R
import com.example.weather_app.apiClasses.CallApi
import com.example.weather_app.databinding.ActivityHomeScreenBinding
import com.example.weather_app.fragments.AdvancedDataFragment
import com.example.weather_app.fragments.BasicDataFragment
import com.example.weather_app.fragments.ForecastFragment


class HomeScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private val apiId: String = "71d31febe6d71d5d79ae8f33e82c55f2"
    private val args = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if(!ifNetworkIsConnected()){
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "Connect to the internet to get current data!", Toast.LENGTH_LONG).show()
        }

        var isClicked = false

        val lastAccessedFile = CallApi().getLastAccessedFile(filesDir.toString())

        if(lastAccessedFile != null && !isClicked){
            Log.d("last accesed file: ", lastAccessedFile.name)
            val lastAccessedFileCityName = CallApi().getCityNameFromLastAccessedFile(lastAccessedFile)
            args.putString("cityName", lastAccessedFileCityName)
            val basicDataFragment = BasicDataFragment()

            basicDataFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, basicDataFragment)
                commit()
            }
        }

        binding.mainCityNameTextView.text = StringBuilder().append("WeatherApp")
        var intent = intent
        val cityName = intent.getStringExtra("cityName")
        if (cityName != null) {
            args.putString("cityName", cityName)
        }

        binding.searchButton.setOnClickListener {
            isClicked = true
            intent = Intent(this, SearchCitiesActivity::class.java)
            startActivity(intent)
        }

        binding.basicDataButton.setOnClickListener {
            isClicked = true
            val basicDataFragment = BasicDataFragment()

            basicDataFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, basicDataFragment)
                commit()
            }
        }

        binding.advancedDataButton.setOnClickListener {
            isClicked = true
            val advancedDataFragment = AdvancedDataFragment()
            advancedDataFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, advancedDataFragment)
                commit()
            }
        }

        binding.forecastButton.setOnClickListener {
            isClicked = true
            val forecastFragment = ForecastFragment()
            forecastFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, forecastFragment)
                commit()
            }
        }

        binding.settingsButton.setOnClickListener {
            val intentSettingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(intentSettingsActivity)
        }

    }
    fun getApiId(): String{
        return apiId
    }
    private fun ifNetworkIsConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetwork != null
    }
}