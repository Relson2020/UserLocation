package com.example.userlocation

import android.Manifest
import android.content.DialogInterface
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.userlocation.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissionId = 1

    // location request
    private val locationRequest = LocationRequest.create().apply {
        interval = 4000
        fastestInterval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // fused location
    private lateinit var fusedLocation: FusedLocationProviderClient

    // locationCallBack
    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            for (location in p0.locations) {
                binding.locationTextView.text = location.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // button onClick listener
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        binding.button.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("log", "hey")
                fusedLocation.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        val longitude = it.longitude
                        Log.i("log", "$longitude")
                        val latitude = it.latitude
                        Log.i("log", "$latitude")
                        val location = "longitude $longitude and latitude $latitude"
                        binding.textView.text = location
                    } else {
                        val text = "Cant get the location"
                        binding.textView.text = text
                    }
                }
            } else {
                getLocationPermission()
            }
        }
    }

    // getLocation
    private fun getLocationPermission() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION,
            )) && (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        ) {

            AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("hey accept the permission to access location")
                .setPositiveButton("ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), permissionId
                        )
                    }
                })
                .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), permissionId
            )
        }
    }

    // check setting for the location
    private fun locationRequestSetting() {
        val locationSettingRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val clientSetting = LocationServices.getSettingsClient(this)
        val task = clientSetting.checkLocationSettings(locationSettingRequest.build())

        Log.i("log", "location setting")

        task.addOnSuccessListener {
            startLocationUpdate()
        }

        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                Log.i("log", "exception")
                try {
                    it.startResolutionForResult(this@MainActivity, 101)
                } catch (e: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun startLocationUpdate() {
        if ((ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            Log.i("log", "request location")
            fusedLocation.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                Looper.getMainLooper()
            )

        }
    }

    override fun onStart() {
        super.onStart()
      //  locationRequestSetting()
        startLocationUpdate()
    }

    override fun onStop() {
        super.onStop()
        fusedLocation.removeLocationUpdates(locationCallBack)
    }
}