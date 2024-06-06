package com.example.weather_app.apiClasses

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.weather_app.activities.HomeScreenActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CallApi {
    fun buildUrl(prefs: SharedPreferences, lat: String, lon: String, whichApi: String): String{
        val type = prefs.getString("type", null)

        Log.d("type!!!!!!!!!!!!!!!!!!: ", type.toString())
        val units = when (type) {
            "°F" -> "imperial"
            "K" -> "standard"
            else -> {"metric"}
        }

        val builder = Uri.Builder()
        builder.scheme("https")
            .authority("api.openweathermap.org")
            .appendPath("data")
            .appendPath("2.5")
            .appendPath(whichApi)
            .appendQueryParameter("lat", lat)
            .appendQueryParameter("lon", lon)
            .appendQueryParameter("appid", HomeScreenActivity().getApiId())
            .appendQueryParameter("units", units)

        return builder.build().toString()
    }

    suspend fun getCall(filesDir: String, apiUrl: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
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
                "Failed"
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun getCityNameFromLastAccessedFile(lastAccessedFile: File): String {
        val fileNameParts = lastAccessedFile.name.split("BasicData")

        return if (fileNameParts.size >= 2) {
            fileNameParts[0]
        } else {
            ""
        }
    }

    fun getLastAccessedFile(filesDir: String): File? {
        val dir = File(filesDir,"weatherApp")
        if (!dir.isDirectory) return null

        val files = dir.listFiles { file -> file.name != "favourites" } ?: return null

        return files.maxByOrNull { it.lastModified() }
    }
}