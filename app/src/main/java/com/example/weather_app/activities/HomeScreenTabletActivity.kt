package com.example.weather_app.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather_app.R
import com.example.weather_app.apiClasses.CallApi
import com.example.weather_app.apiClasses.Refresher
import com.example.weather_app.databinding.ActivityHomeScreenTabletBinding
import com.example.weather_app.fragments.AdvancedDataFragment
import com.example.weather_app.fragments.BasicDataTabletFragment
import com.example.weather_app.fragments.ForecastFragment

class HomeScreenTabletActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenTabletBinding
    private val args = Bundle()
    private val refresher = Refresher(this, args)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenTabletBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if(!refresher.getIsRunning()){
            Log.d("Started", "Started")
            refresher.run()
        }

        if(!ifNetworkIsConnected()){
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "Connect to the internet to get current data!", Toast.LENGTH_LONG).show()
        }

        var isClicked = false

        val lastAccessedFile = CallApi(applicationContext).getLastAccessedFile()

        if(lastAccessedFile != null && !isClicked){
            Log.d("last accesed file: ", lastAccessedFile.name)
            val lastAccessedFileCityName = CallApi(applicationContext).getCityNameFromLastFileName(lastAccessedFile.name)
            args.putString("cityName", lastAccessedFileCityName)

            val basicDataTabletFragment = BasicDataTabletFragment()
            val advancedDataFragment = AdvancedDataFragment()
            val forecastFragment = ForecastFragment()

            basicDataTabletFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.basicDataFrameLayout.id, basicDataTabletFragment, "BasicDataTabletFragment")
                commit()
            }

            advancedDataFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.advancedDataFrameLayout.id, advancedDataFragment, "AdvancedDataFragment")
                commit()
            }

            forecastFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.forecastFrameLayout.id, forecastFragment, "ForecastFragment")
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

        binding.settingsButton.setOnClickListener {
            val intentSettingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(intentSettingsActivity)
        }

    }
    private fun ifNetworkIsConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetwork != null
    }
    override fun onResume() {
        super.onResume()
        refresher.setResumeTime()
    }

    override fun onStop() {
        super.onStop()
        refresher.setStopTime()
    }
}