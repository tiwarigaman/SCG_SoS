package com.mobile.scgsos

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(applicationContext, "AIzaSyCmxi1Tlu8dLWIdIG-DT-yk9NgLZ5piaGo")
        placesClient = Places.createClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Check if location services are enabled
        if (isLocationEnabled()) {
            // Your existing code...
            val a = 0
            // Set up the button click listener
            val showLocationButton: FloatingActionButton = findViewById(R.id.showLocationButton)
            showLocationButton.setOnClickListener {
                // Check for location permission
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    enableMyLocation()
                } else {
                    // Request location permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }

            // Set up map click listener to place a marker
            mMap.setOnMapClickListener { latLng ->
                // Clear previous markers
                mMap.clear()

                // Add a marker at the clicked location
                val marker =
                    mMap.addMarker(MarkerOptions().position(latLng).title("Clicked Location"))

                // Move the camera to the clicked location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Set up marker click listener to show place details
                mMap.setOnMarkerClickListener { clickedMarker ->
                    if (clickedMarker == marker) {
                        showPlaceDetails(latLng)
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
        // Function to show details of a place
        private fun showPlaceDetails(latLng: LatLng) {
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)

            val request = FetchPlaceRequest.builder(latLng.toLocation().toString(), placeFields).build()

            placesClient.fetchPlace(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response: FetchPlaceResponse = task.result!!

                    val place = response.place

                    // Access the details of the place and display them
                    val placeName = place.name
                    val placeAddress = place.address

                    // Display the details (you can customize this based on your UI)
                    Toast.makeText(
                        this@MainActivity,
                        "Place: $placeName, Address: $placeAddress",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Handle the error
                    val exception = task.exception
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        Log.e(TAG, "Error fetching place details. Status code: $statusCode")
                    } else {
                        Log.e(TAG, "Error fetching place details.", exception)
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch place details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun LatLng.toLocation(): String {
        return "${latitude},${longitude}"
    }




    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            location?.let {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // Clear previous markers
                mMap.clear()

                // Add a marker for the current location
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))

                // Move the camera to the current location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }

        // Set an OnMyLocationButtonClickListener to handle button clicks
        mMap.setOnMyLocationButtonClickListener {
            // Handle the click event, e.g., custom behavior or return false to let the default behavior
            // In this example, we return false to let the default behavior center the camera on the user's location
            false
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Location Services Disabled")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Location Permission Denied")
            .setMessage("Please enable location services and grant location permission to use this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
