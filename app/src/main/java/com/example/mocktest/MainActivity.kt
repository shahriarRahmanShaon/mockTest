package com.example.mocktest

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import com.google.android.material.snackbar.Snackbar

import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission


class MainActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var button1 = findViewById<Button>(R.id.button)
        var textj = findViewById<TextView>(R.id.textView)
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
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // The permission is granted
                    // you can go with the flow that requires permission here
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            // Got last known location. In some rare situations this can be null.
                            var lat = location!!.latitude
                            print(lat)
                            textj.setText(lat.toString())
                        }
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                    // This case means user previously denied the permission
                    // So here we can display an explanation to the user
                    // That why exactly we need this permission
                    Toast.makeText(this, "message", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                else -> {
                    // Everything is fine you can simply request the permission
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
        }

    }


}