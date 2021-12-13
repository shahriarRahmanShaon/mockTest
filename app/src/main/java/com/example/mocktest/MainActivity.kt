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



          requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Permission: ", "Granted")

                } else {
                    Log.i("Permission: ", "Denied")
                }
            }
        button1.setOnClickListener{
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // The permission is granted
                    // you can go with the flow that requires permission here
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            // Got last known location. In some rare situations this can be null.
                            var lat = location!!.latitude
                            var lon = location!!.longitude
                            Log.d("json", lat.toString())
                            Log.d("json", lon.toString())
                            var cityName = getAddress(lat, lon)
                            runOnUiThread {
                                textj.setText(cityName)
                            }
                            getWeather(cityName)
                        }
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // This case means user previously denied the permission
                    // So here we can display an explanation to the user
                    // That why exactly we need this permission
                    Toast.makeText(this, "message", Toast.LENGTH_SHORT).show()
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
        var url = "https://api.openweathermap.org/data/2.5/weather?q=${cityName}&appid=0e467e5c6beceddf2be0be537b297025"
        var request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    var getString = response.body!!.string()
                    var gson = GsonBuilder().create()
                    var result =  gson.fromJson(getString, Welcome::class.java)
                    Log.d("json", result.weather[0].id.toString())
                    runOnUiThread {
                        text2.setText(result.main.temp.toString())
                    }

                    getBackground(result.weather[0].icon)
                    Log.d("json", result.weather[0].icon)
                }
            }
        })

    }

    private fun getBackground(iconId: String) {
        var url = "https://openweathermap.org/img/wn/${iconId}@2x.png"
        runOnUiThread {
            Glide.with(this).load(url).into(imageView);
        }
    }

    private fun getAddress(lat: Double, lng: Double): String {
        var geocoder = Geocoder(this)
        var list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].locality
    }


}