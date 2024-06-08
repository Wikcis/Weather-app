package com.example.weather_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.weather_app.R
import com.example.weather_app.apiClasses.ApiResponseFetcher
import com.example.weather_app.databinding.FragmentAdvancedDataBinding
import com.example.weather_app.models.BasicDataResponseModel

class AdvancedDataFragment : Fragment(R.layout.fragment_advanced_data) {
    private lateinit var binding: FragmentAdvancedDataBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdvancedDataBinding.inflate(layoutInflater)
        val view = binding.root
        val cityName = arguments?.getString("cityName")
        val basicResponse: BasicDataResponseModel = ApiResponseFetcher().fetchDataFromApi("${cityName}BasicData.json", requireContext())
        binding.windPowerTextView.text = StringBuilder().append("${basicResponse.wind.speed} m/s")

        binding.windDirectionImageView.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.arrow))

        binding.windDirectionImageView.rotation = -basicResponse.wind.deg.toFloat()

        binding.humidityTextView.text = StringBuilder().append("Humidity: ${basicResponse.main.humidity} %")
        binding.visibilityTextView.text = StringBuilder().append("Visibility: ${basicResponse.visibility} m")
        binding.sunriseTextView.text = StringBuilder().append("Sunrise: ${ApiResponseFetcher().getTimeFromTimeStamp(basicResponse.sys.sunrise + basicResponse.timezone)}")
        binding.sunsetTextView.text = StringBuilder().append("Sunset: ${ApiResponseFetcher().getTimeFromTimeStamp(basicResponse.sys.sunset + basicResponse.timezone)}")

        val activity = requireActivity()
        if(activity.localClassName.compareTo("activities.HomeScreenTabletActivity") != 0){
            activity.findViewById<ImageView>(R.id.advancedDataButton).setImageResource(R.drawable.refresh)

        }
        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity()
        if(activity.localClassName.compareTo("activities.HomeScreenTabletActivity") != 0){
            activity.findViewById<ImageView>(R.id.advancedDataButton).setImageResource(R.drawable.wind)
        }
    }
}