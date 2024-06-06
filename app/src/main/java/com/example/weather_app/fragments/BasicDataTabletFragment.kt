package com.example.weather_app.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.example.weather_app.R
import com.example.weather_app.apiClasses.ApiResponseFetcher
import com.example.weather_app.databinding.FragmentBasicDataTabletBinding
import com.example.weather_app.models.BasicDataResponseModel

class BasicDataTabletFragment : Fragment() {
    private lateinit var binding: FragmentBasicDataTabletBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBasicDataTabletBinding.inflate(layoutInflater)
        val view = binding.root
        val cityName = arguments?.getString("cityName")

        val basicResponse: BasicDataResponseModel = ApiResponseFetcher().fetchDataFromApi( "${cityName}BasicData.json", requireContext())

        val activity = requireActivity()

        activity.findViewById<TextView>(R.id.mainCityNameTextView).text = basicResponse.name

        val prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val type = prefs.getString("type", "°C")

        binding.tempTextView.text = StringBuilder().append("${basicResponse.main.temp} $type")
        binding.latTextView.text = StringBuilder().append("Coords: ${basicResponse.coord.lat}")
        binding.lonTextView.text = StringBuilder().append("${basicResponse.coord.lon}")

        binding.timeTextView.text = ApiResponseFetcher().getCurrentTimeFromTimezone(basicResponse.timezone)
        binding.pressureTextView.text = StringBuilder().append("${basicResponse.main.pressure} hPa")
        binding.descriptionTextView.text = basicResponse.weather[0].main

        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${basicResponse.weather[0].icon}@2x.png")
            .into(binding.descImageView)

        return view
    }
}