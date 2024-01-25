package com.mobile.scgsos

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperColors.fromDrawable
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.*



class MainActivity2 : AppCompatActivity() {
    private lateinit var mapView: com.mapbox.maps.MapView
    private lateinit var floatingActionButton: FloatingActionButton


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        mapView = findViewById(R.id.mapView2)
        floatingActionButton = findViewById(R.id.focusLocation)

        if (ActivityCompat.checkSelfPermission(this@MainActivity2, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        floatingActionButton.hide()
        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE) { style ->
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().zoom(20.0).build())
            val locationComponentPlugin = mapView.location
            locationComponentPlugin.enabled = true
            val locationPuck2D = LocationPuck2D()

            locationPuck2D.bearingImage =
                ImageHolder.Companion.from(R.drawable.ic_baseline_location_on_24)
            locationComponentPlugin.locationPuck = locationPuck2D
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            getGestures(mapView)?.addOnMoveListener(onMoveListener)

            floatingActionButton.setOnClickListener {
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
                getGestures(mapView)?.addOnMoveListener(onMoveListener)
                floatingActionButton.hide()
            }
        }
    }
    private val activityResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result: Boolean ->
            if (result) {
                Toast.makeText(this@MainActivity2, "Permission granted!", Toast.LENGTH_SHORT).show()
            }
        }

    private val onIndicatorBearingChangedListener =
        OnIndicatorBearingChangedListener { v ->
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(v).build())
        }

    private val onIndicatorPositionChangedListener =
        OnIndicatorPositionChangedListener { point ->
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(20.0)
                    .build()
            )
            getGestures(mapView)?.focalPoint = mapView.getMapboxMap().pixelForCoordinate(point)
        }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(moveGestureDetector: MoveGestureDetector) {
            getLocationComponent(mapView)?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            getLocationComponent(mapView)?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            getGestures(mapView)?.removeOnMoveListener(this)
            floatingActionButton.show()
        }

        override fun onMove(moveGestureDetector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {}
    }

    private fun getLocationComponent(mapView: MapView): LocationComponentPlugin? {
        return mapView.location
    }

    private fun getGestures(mapView: MapView): com.mapbox.maps.plugin.gestures.GesturesPlugin? {
        return mapView.gestures
    }
}
