package com.example.mocktest

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var client = OkHttpClient()
    lateinit var text2: TextView
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var button1 = findViewById<Button>(R.id.button)
        var textj = findViewById<TextView>(R.id.textView)
        text2 = findViewById<TextView>(R.id.textView2)
        imageView = findViewById(R.id.imageView2)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        requestPermissionLauncher = registerForActivityResult( ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Permission: ", "Granted")

                } else {
                    Log.i("Permission: ", "Denied")
                }
            }
        button1.setOnClickListener{
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                    // The permission is granted
                    // doing the task that needs location permission

                    fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                            // Got last known location with listener in it ignoring error checking

                            var lat = location!!.latitude  // we're sure we gonna get a response, so used !! but not recommended
                            var lon = location!!.longitude

                        // printing to console for checking
                            Log.d("json", lat.toString())
                            Log.d("json", lon.toString())

                        // calling getAdress fucntion with lat and long parameter passed
                            var cityName = getAddress(lat, lon)

                            runOnUiThread {
                                // changing UI in main thread
                                textj.setText(cityName)
                            }
                        // calling the openWeather Api with city name in int
                            getWeather(cityName)
                        }
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(this, "message", Toast.LENGTH_SHORT).show() // showing message why we need location info
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    // Everything is fine you can simply request the permission
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

    }

    private fun getWeather(cityName: String) {
        //function to fetch weather data from openWeatherApi using OKHttps3

        var url = "https://api.openweathermap.org/data/2.5/weather?q=${cityName}&appid=0e467e5c6beceddf2be0be537b297025" // making dynamic api url and personal key
        var request = Request.Builder() // making a builder request with url just made above
            .url(url)
            .build()
        // making request call
        client.newCall(request).enqueue(object : Callback {
            // Acctually making a request constantly which return two callback named onFailure and Onresponse

            override fun onFailure(call: Call, e: IOException) {
                // We simply catch the error

                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Do the things we plan to do if the request is successful

                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    var getString = response.body!!.string() // get the response body from response and map that into string

                    // now we need to map the response into dataclass object
                    var gson = GsonBuilder().create() // build gson instance
                    var result =  gson.fromJson(getString, Welcome::class.java) // map the response into welcome.kt data class
                    Log.d("json", result.weather[0].id.toString()) // printing id of the json response to console for test
                    runOnUiThread {
                        // changing UI int he main thread
                        text2.setText(result.main.temp.toString())
                    }

                    getBackground(result.weather[0].icon) // calling getBackgroud function with icon parameter
                    Log.d("json", result.weather[0].icon)
                }
            }
        })
    }

    private fun getBackground(iconId: String) {
        // Function to get background image from openWeatherApi using glide

        var url = "https://openweathermap.org/img/wn/${iconId}@2x.png" // making dynamic url
        runOnUiThread {
            // As this is an UI element modification it must be done on the main thread
            Glide.with(this).load(url).into(imageView);
        }
    }

    private fun getAddress(lat: Double, lng: Double): String {
        // Function to get location city name using geocoder
        var geocoder = Geocoder(this) // initializing geocoder for the context
        var list = geocoder.getFromLocation(lat, lng, 1) // getting location using lat long
        return list[0].locality  // as it returns a list object we fetch only the local name
    }


}