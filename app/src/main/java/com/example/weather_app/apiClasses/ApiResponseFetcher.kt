package com.example.weather_app.apiClasses

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.sql.Timestamp
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class ApiResponseFetcher {
    inline fun <reified T> fetchDataFromApi(fileName: String, context: Context): T {
        val dir = File(context.filesDir,"weatherApp")
        val file = File(dir, fileName)
        val response = StringBuilder()
        try{
            val inputStream = FileInputStream(file)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                response.append(line).append("\n")
                line = reader.readLine()
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
        val jsonString = response.toString()
        return parseJsonToModel<T>(jsonString)
    }
    inline fun <reified T> parseJsonToModel(jsonString: String): T{
        val gson = Gson()
        return gson.fromJson(jsonString, object : TypeToken<T>() {}.type)
    }
    fun getCurrentTimeFromTimezone(timezone: Int): String {
        val zoneOffset = ZoneOffset.ofTotalSeconds(timezone)
        val currentTimeUTC = ZonedDateTime.now(ZoneOffset.UTC)
        val currentTimeOffset = currentTimeUTC.withZoneSameInstant(zoneOffset)
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return formatter.format(currentTimeOffset)
    }
    fun getTimeFromTimeStamp(timestamp: Int): String {
        return try {
            val stamp = Timestamp(timestamp.toLong() * 1000)
            val cal = Calendar.getInstance()
            cal.time = Date(stamp.time)
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            val formattedMinute = "%02d".format(minute)

            "$hour:$formattedMinute"
        } catch (e: Exception) {
            e.toString()
        }
    }
}