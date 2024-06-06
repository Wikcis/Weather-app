package com.example.weather_app.activities

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather_app.apiClasses.CallApi
import com.example.weather_app.R
import com.example.weather_app.databinding.ActivitySearchCitiesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


class SearchCitiesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchCitiesBinding
    private var isAccessed = true
    private lateinit var favouriteCitiesNames: ArrayList<String>
    private lateinit var cityNames: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchCitiesBinding.inflate(layoutInflater)

        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        favouriteCitiesNames = ArrayList()
        cityNames = ArrayList()
        var dir = File(filesDir, "weatherApp/favourites")
        if(!dir.exists()){
            dir.mkdirs()
        }
        val fileFav = File(dir, "favouriteCities.txt")
        if(!fileFav.exists()){
            fileFav.createNewFile()
        }
        try{
            val inputStream = FileInputStream(fileFav)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                if(line.isNotBlank())
                    favouriteCitiesNames.add(line)
                line = reader.readLine()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        dir = File(filesDir, "weatherApp")

        if (dir.exists() && dir.isDirectory) {
            readAllFilesFromDir(dir)

        } else {
            Log.d("Error dir","Directory does not exist or is not a directory.")
        }

        val tempInflater = LayoutInflater.from(this)

        makeFavouriteCitiesViews(tempInflater, fileFav)

        binding.searchButton.setOnClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (inputMethodManager.isActive) {
                inputMethodManager.hideSoftInputFromWindow(binding.searchBarEditText.windowToken, 0)
            }

            val userCityName = binding.searchBarEditText.text.toString()
            Log.d("City name: ", userCityName)

            if(userCityName.isBlank()){
                Toast.makeText(this, "Type in your location!", Toast.LENGTH_SHORT).show()

                if(!isAccessed){
                    binding.containerLayout.removeAllViews()

                    makeFavouriteCitiesViews(tempInflater, fileFav)
                }
            }else{
                val args = Bundle()
                args.putString("cityName", userCityName)
                val geocoder = Geocoder(this)

                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        val coords = geocoder.getFromLocationName(userCityName, 1)
                        if (!coords.isNullOrEmpty()) {
                            binding.containerLayout.removeAllViews()

                            val lat = coords[0]?.latitude.toString()
                            val lon = coords[0]?.longitude.toString()

                            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

                            val basicDataFileDeferred = async {
                                val url = CallApi().buildUrl(prefs, lat, lon, "weather")
                                CallApi().getCall(filesDir.toString() ,url, "${userCityName}BasicData.json")
                            }

                            val hourlyForecastFileDeferred = async {
                                val url = CallApi().buildUrl(prefs, lat, lon, "forecast")
                                CallApi().getCall(filesDir.toString(), url, "${userCityName}BasicDataHourlyForecast.json")
                            }

                            val basicDataFile = basicDataFileDeferred.await()
                            val hourlyForecastFile = hourlyForecastFileDeferred.await()

                            if (basicDataFile == "Failed" || hourlyForecastFile == "Failed") {
                                Log.d("File creation failed", "Failed to create one or more files")
                            }
                            val intent = Intent(applicationContext, LauncherActivity::class.java)
                            intent.putExtra("cityName", userCityName)
                            startActivity(intent)
                        }else{
                            Toast.makeText(applicationContext, "Wrong city name!", Toast.LENGTH_SHORT).show()
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.historyCitiesButton.setOnClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (inputMethodManager.isActive) {
                inputMethodManager.hideSoftInputFromWindow(binding.searchBarEditText.windowToken, 0)
            }

            isAccessed = false
            binding.containerLayout.removeAllViews()

            makeHistoryCitiesViews(tempInflater, fileFav)
        }
    }

    private fun readAllFilesFromDir(dir: File) {
        val files = dir.listFiles { file -> file.name.contains("BasicData.json")}
        files?.forEach { file ->
            val onlyCityName = file.name.removeSuffix("BasicData.json")
            if(!favouriteCitiesNames.contains(onlyCityName))
                cityNames.add(onlyCityName)
        }
    }

    private fun makeFavouriteCitiesViews(tempInflater: LayoutInflater, fileFav: File) {
        for (i in favouriteCitiesNames.indices) {
            val itemView = tempInflater.inflate(R.layout.item_favourite_cities, binding.containerLayout, false)

            val favCityNameButton = itemView.findViewById<Button>(R.id.favCityNameButton)
            val removeCityFromFavouritesButton = itemView.findViewById<Button>(R.id.removeCityFromFavouritesButton)

            favCityNameButton.text = favouriteCitiesNames[i]

            favCityNameButton.setOnClickListener {
                Log.d("giving city name: ", favouriteCitiesNames[i])
                val intent = Intent(applicationContext, LauncherActivity::class.java)
                intent.putExtra("cityName", favouriteCitiesNames[i])
                startActivity(intent)
            }

            removeCityFromFavouritesButton.setOnClickListener {
                val index = favouriteCitiesNames.indexOf(favouriteCitiesNames[i])
                favouriteCitiesNames.removeAt(index)

                val lines = StringBuilder()
                if(favouriteCitiesNames.isNotEmpty()) {
                    fileFav.forEachLine { line ->
                        if (line.trim().compareTo(favouriteCitiesNames[i]) != 0) {
                            lines.append(line + "\n")
                        }
                    }
                }

                fileFav.writeText(lines.toString())

                binding.containerLayout.removeView(itemView)
            }

            binding.containerLayout.addView(itemView)
        }
    }

    private fun makeHistoryCitiesViews(tempInflater: LayoutInflater, fileFav: File) {
        for (i in cityNames.indices) {
            if(!favouriteCitiesNames.contains(cityNames[i])){
                val itemView = tempInflater.inflate(R.layout.item_histiry_cities, binding.containerLayout, false)

                val favCityNameButton = itemView.findViewById<Button>(R.id.favCityNameButton)
                val addToFavButton = itemView.findViewById<Button>(R.id.addCityToFavouritesButton)

                favCityNameButton.text = cityNames[i]

                favCityNameButton.setOnClickListener {
                    Log.d("giving city name: ", cityNames[i])

                    val intent = Intent(applicationContext, LauncherActivity::class.java)
                    intent.putExtra("cityName", cityNames[i])
                    startActivity(intent)
                }

                addToFavButton.setOnClickListener {
                    favouriteCitiesNames.add(cityNames[i])
                    val reader = fileFav.readText()
                    if(!reader.contains(cityNames[i])){
                        fileFav.appendText(cityNames[i]+"\n")
                    }

                    binding.containerLayout.removeView(itemView)
                }

                binding.containerLayout.addView(itemView)
            }
        }
    }
}