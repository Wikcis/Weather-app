package com.example.weather_app.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.weather_app.R
import com.example.weather_app.apiClasses.ApiResponseFetcher
import com.example.weather_app.databinding.FragmentBasicDataBinding
import com.example.weather_app.models.HourlyForecastResponseModel
import com.example.weather_app.models.BasicDataResponseModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.round

class BasicDataFragment : Fragment(R.layout.fragment_basic_data) {
    private lateinit var binding: FragmentBasicDataBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBasicDataBinding.inflate(layoutInflater)
        val view = binding.root
        val cityName = arguments?.getString("cityName")

        val basicResponse: BasicDataResponseModel = ApiResponseFetcher().fetchDataFromApi( "${cityName}BasicData.json", requireContext())
        val hourlyResponse: HourlyForecastResponseModel = ApiResponseFetcher().fetchDataFromApi("${cityName}BasicDataHourlyForecast.json", requireContext())

        val activity = requireActivity()
        activity.findViewById<TextView>(R.id.mainCityNameTextView).text = basicResponse.name

        val prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val type = prefs.getString("type", "°C")

        binding.tempTextView.text = StringBuilder().append("${round(basicResponse.main.temp.toString().toDouble()).toInt()} $type")
        binding.latTextView.text = StringBuilder().append("Coords: ${basicResponse.coord.lat}")
        binding.lonTextView.text = StringBuilder().append("${basicResponse.coord.lon}")

        binding.timeTextView.text = ApiResponseFetcher().getCurrentTimeFromTimezone(basicResponse.timezone)
        binding.pressureTextView.text = StringBuilder().append("${basicResponse.main.pressure} hPa")
        binding.descriptionTextView.text = basicResponse.weather[0].main

        activity.findViewById<ImageView>(R.id.basicDataButton).setImageResource(R.drawable.refresh)


        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val containerLayout = view.findViewById<LinearLayout>(R.id.container_layout)

        val hours = ArrayList<String>()
        val daysOfWeek = ArrayList<String>()
        hourlyResponse.list.take(10).forEach { item ->
            val dtTxt = item.dtTxt
            val hour = LocalDateTime.parse(dtTxt, formatter).hour
            val dayOfWeek = if(hour == 0){
               LocalDateTime.parse(dtTxt, formatter).dayOfWeek
            }else ""
            daysOfWeek.add(dayOfWeek.toString())
            hours.add("$hour:00")
        }

        val temps = ArrayList<String>()
        hourlyResponse.list.take(10).forEach { item->
            val temp = item.main.temp
            temps.add("${round(temp.toString().toDouble()).toInt()} $type")
        }

        val icons = ArrayList<String>()

        hourlyResponse.list.take(10).forEach { icon->
             icons.add(icon.weather[0].icon)
        }

        val tempInflater = LayoutInflater.from(requireContext())

        for (i in hours.indices) {
            val itemView = tempInflater.inflate(R.layout.item_temp, containerLayout, false)

            val hourTextView = itemView.findViewById<TextView>(R.id.hourTextView)
            val tempTextView = itemView.findViewById<TextView>(R.id.tempTextView)
            val imageView = itemView.findViewById<ImageView>(R.id.imageView)
            val dayOfWeekTextView = itemView.findViewById<TextView>(R.id.dayOfWeekTextView)
            dayOfWeekTextView.text = daysOfWeek[i]
            hourTextView.text = hours[i]
            tempTextView.text = temps[i]
            Glide.with(this)
                .load("https://openweathermap.org/img/wn/${icons[i]}@2x.png")
                .into(imageView)

            containerLayout.addView(itemView)
        }

        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${basicResponse.weather[0].icon}@2x.png")
            .into(binding.descImageView)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity()
        activity.findViewById<ImageView>(R.id.basicDataButton).setImageResource(R.drawable.temperature)
    }
}