package com.example.weather_app.apiClasses

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.weather_app.activities.HomeScreenActivity
import com.example.weather_app.activities.LauncherActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CallApi(private val context: Context) {
    private val filesDir = context.filesDir
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private suspend fun getCords(fileName: String): Cords {
        val geocoder = Geocoder(context)
        var lat = ""
        var lon = ""
        val coords = withContext(Dispatchers.IO) {
            geocoder.getFromLocationName(fileName, 1)
        }
        if (!coords.isNullOrEmpty()) {
            lat = coords[0].latitude.toString()
            lon = coords[0].longitude.toString()
        }
        return Cords(lat, lon)
    }

    private suspend fun buildUrl(fileName: String, whichApi: String): String {
        val cords = getCords(fileName)

        val type = prefs.getString("type", null)

        val units = when (type) {
            "°F" -> "imperial"
            "K" -> "standard"
            else -> "metric"
        }

        val builder = Uri.Builder()
        builder.scheme("https")
            .authority("api.openweathermap.org")
            .appendPath("data")
            .appendPath("2.5")
            .appendPath(whichApi)
            .appendQueryParameter("lat", cords.lat)
            .appendQueryParameter("lon", cords.lon)
            .appendQueryParameter("appid", withContext(Dispatchers.Main) {
                HomeScreenActivity().getApiId()
            })
            .appendQueryParameter("units", units)

        return builder.build().toString()
    }

    suspend fun getCall(fileName: String, whichApi: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL(buildUrl(fileName, whichApi))
            var connection: HttpURLConnection? = null
            var response = ""

            try {
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = InputStreamReader(connection.inputStream)
                    val bufferedReader = BufferedReader(reader)
                    response = bufferedReader.use(BufferedReader::readText)

                    var dir = File("${filesDir}/weatherApp/favourites")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    dir = File("${filesDir}/weatherApp")

                    val file = File(dir, fileName)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(response.toByteArray())
                    fileOutputStream.close()
                }
                Log.d("success: ", response)
                response
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Failed", "0")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }

                "Failed"
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun searchCity(): Boolean {
        var res = false
        try {
            val lastAccessedFile = getLastAccessedFile()
            if (lastAccessedFile != null) {
                val userCityName = getCityNameFromLastFileName(lastAccessedFile.name)
                CoroutineScope(Dispatchers.Main).launch {
                    val basicDataFileDeferred = async(Dispatchers.IO) {
                        getCall("${userCityName}BasicData.json", "weather")
                    }

                    val hourlyForecastFileDeferred = async(Dispatchers.IO) {
                        getCall("${userCityName}BasicDataHourlyForecast.json", "forecast")
                    }

                    val basicDataFile = basicDataFileDeferred.await()
                    val hourlyForecastFile = hourlyForecastFileDeferred.await()

                    if (basicDataFile == "Failed" || hourlyForecastFile == "Failed") {
                        Log.d("File creation failed", "Failed to create one or more files")
                    } else {
                        val intent = Intent(context, LauncherActivity::class.java)
                        intent.putExtra("cityName", userCityName)
                        context.startActivity(intent)
                        res = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return res
    }

    fun getCityNameFromLastFileName(lastAccessedFileName: String): String {
        val fileNameParts = lastAccessedFileName.split("BasicData")

        return if (fileNameParts.size >= 2) {
            fileNameParts[0]
        } else {
            ""
        }
    }

    fun getLastAccessedFile(): File? {
        val dir = File(filesDir, "weatherApp")
        if (!dir.isDirectory) return null

        val files = dir.listFiles { file -> file.name != "favourites" } ?: return null

        return files.maxByOrNull { it.lastModified() }
    }
}
