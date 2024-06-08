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
import androidx.fragment.app.Fragment
import com.example.weather_app.R
import com.example.weather_app.apiClasses.CallApi
import com.example.weather_app.apiClasses.Refresher
import com.example.weather_app.databinding.ActivityHomeScreenBinding
import com.example.weather_app.fragments.AdvancedDataFragment
import com.example.weather_app.fragments.BasicDataFragment
import com.example.weather_app.fragments.ForecastFragment

class HomeScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private val apiId: String = "71d31febe6d71d5d79ae8f33e82c55f2"
    private val args = Bundle()
    private val refresher = Refresher(this, args)
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
            Log.d("last accessed file: ", lastAccessedFile.name)
            val lastAccessedFileCityName = CallApi(applicationContext).getCityNameFromLastFileName(lastAccessedFile.name)
            args.putString("cityName", lastAccessedFileCityName)
            val basicDataFragment = BasicDataFragment()

            basicDataFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, basicDataFragment, "BasicDataFragment")
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
                replace(binding.frameLayout.id, basicDataFragment, "BasicDataFragment")
                commit()
            }
        }

        binding.advancedDataButton.setOnClickListener {
            isClicked = true
            val advancedDataFragment = AdvancedDataFragment()
            advancedDataFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, advancedDataFragment, "AdvancedDataFragment")
                commit()
            }
        }

        binding.forecastButton.setOnClickListener {
            isClicked = true
            val forecastFragment = ForecastFragment()
            forecastFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, forecastFragment, "ForecastFragment")
                commit()
            }
        }

        binding.settingsButton.setOnClickListener {
            val intentSettingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(intentSettingsActivity)
        }

    }
    fun startFragment(fragment: Fragment, args: Bundle){
        fragment.arguments = args

        val fragmentParts = fragment.toString().split("Fragment")
        var fragmentName = ""
        if (fragmentParts.size >= 2) {
            fragmentName = fragmentParts[0] + "Fragment"
        }

        Log.d("Fragment name: ", fragment.toString())
        supportFragmentManager.beginTransaction().apply {
            replace(binding.frameLayout.id, fragment, fragmentName)
            commit()
        }
    }
    fun isFragmentVisible(tag: String): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        return fragment != null && fragment.isVisible
    }
    fun getApiId(): String{
        return apiId
    }
    private fun ifNetworkIsConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetwork != null
    }
    override fun onResume() {
        super.onResume()
        Log.d("resume", "resume")
        refresher.setResumeTime()
    }

    override fun onStop() {
        super.onStop()
        Log.d("stop", "stop")

        refresher.setStopTime()
    }
}