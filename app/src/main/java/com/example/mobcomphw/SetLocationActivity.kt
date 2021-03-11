package com.example.mobcomphw

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderApi.KEY_MOCK_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.FusedLocationProviderClient.KEY_VERTICAL_ACCURACY
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

const val KEY_MOCK_LOCATION = true
const val KEY_VERTICAL_ACCURACY = 1.0f

class SetLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var ouluLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.mobcomphw.R.layout.maps_activity)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.mobcomphw.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        if (!isLocationPermissionGranted()) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                LOCATION_REQUEST_CODE
            )
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return
            }
            this.map.isMyLocationEnabled = true
        }

        // SET VIRTUAL LOCATION; APP MUST BE SET TO MOCK LOCATION PROVIDER IN DEV OPTIONS
        fusedLocationClient.setMockMode(BuildConfig.DEBUG)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : android.location.Location? ->
                Log.d("Lab", "Set mock mode on")
            }
            .addOnFailureListener {  Log.d("Lab", "Set mock mode failed") }

        setLongClick(map)

        // Zoom to last location
        fusedLocationClient.lastLocation.addOnSuccessListener {
            // Got last known location. In some rare situations this can be null.
            if (it != null) {
                with(map) {
                    val latLng = LatLng(it.latitude, it.longitude)
                    setMockLocation(latLng)
                    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
                }
            } else {
                val mockLatLng : LatLng = LatLng(65.01, 25.47)
                setMockLocation(mockLatLng)
                with(map) {
                    moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            mockLatLng,
                            CAMERA_ZOOM_LEVEL
                        )
                    )
                }
            }
        }
    }

    private fun setMockLocation(latLng: LatLng) {
        val mockLocation : Location = Location("newLoc")
        mockLocation.setLongitude(latLng.longitude)
        mockLocation.setLatitude(latLng.latitude)
        mockLocation.altitude = 3.0
        mockLocation.time = System.currentTimeMillis()
        mockLocation.speed = 0.01f
        mockLocation.bearing = 1f
        mockLocation.accuracy = 3f
        mockLocation.isFromMockProvider
        mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mockLocation.bearingAccuracyDegrees = 0.1f
            mockLocation.verticalAccuracyMeters = 0.1f
            mockLocation.speedAccuracyMetersPerSecond = 0.01f
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.setMockLocation(mockLocation)
            .addOnSuccessListener { Log.d("Lab", "Location mocked to $mockLocation") }
            .addOnFailureListener { Log.d("Lab", "mock failed") }
    }

    private fun setLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latlng ->
            map.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .alpha(0f)
                    .title("Virtual location set here!")
            ).showInfoWindow()
            setMockLocation(latlng)
        }
    }

    private fun isLocationPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE) {
            if (permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "This application needs background location to work on Android 10 and higher",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (
                grantResults.isNotEmpty() && (
                        grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                                grantResults[1] == PackageManager.PERMISSION_GRANTED)
            ) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                map.isMyLocationEnabled = true
                onMapReady(map)
            } else {
                Toast.makeText(
                    this,
                    "The app needs location permission to function",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (grantResults.isNotEmpty() && grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "This application needs background location to work on Android 10 and higher",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}