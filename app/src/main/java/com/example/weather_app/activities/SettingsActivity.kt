package com.example.weather_app.activities

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather_app.R
import com.example.weather_app.apiClasses.CallApi
import com.example.weather_app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.refreshButton.setOnClickListener {
            val lastAccessedFile = CallApi().getLastAccessedFile(filesDir.toString())
            if (lastAccessedFile != null) {
                val intent = Intent(applicationContext, LauncherActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, "There is no data to refresh!", Toast.LENGTH_LONG).show()
            }
        }

        binding.changeLocationButton.setOnClickListener {
            val intent = Intent(applicationContext, SearchCitiesActivity::class.java)
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            val intent = Intent(applicationContext, LauncherActivity::class.java)
            startActivity(intent)
        }

        val types = arrayOf("°C", "°F", "K")

        val arrayAdp = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, types)

        binding.spinner.adapter = arrayAdp

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val prefs = getSharedPreferences("settings",Context.MODE_PRIVATE)
                prefs.edit().putString("type", types[position]).commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }
}