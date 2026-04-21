package com.example.projekat.location

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel(){
    private var fusedLocationClient : FusedLocationProviderClient? = null
    private var locationCallback : LocationCallback? = null
    private val _location = MutableStateFlow<Location?>(null)
    val location : StateFlow<Location?> = _location
    var gpsAlpha = 0.3f
    private var lastLat: Double = 0.0
    private var lastLon: Double = 0.0
    @Suppress("MissingPermission")
    fun startLocationUpdates (context: Context){
        if (fusedLocationClient == null){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }
        locationCallback?.let{
            fusedLocationClient?.removeLocationUpdates { it }
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L)
            .setMinUpdateIntervalMillis(200L)
            .setMinUpdateDistanceMeters(0f)
            .setWaitForAccurateLocation(false)
            .build()
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let{ loc ->
                    if (_location.value == null) {
                        lastLat = loc.latitude
                        lastLon = loc.longitude
                        viewModelScope.launch {
                            _location.value = loc
                        }
                        return
                    }

                        val stabilizedLat = lowPassFilter(loc.latitude, gpsAlpha, lastLat)
                        val stabilizedLon = lowPassFilter(loc.longitude, gpsAlpha, lastLon)
                        lastLat = stabilizedLat
                        lastLon = stabilizedLon
                        val stabilizedLocation = Location(loc.provider).apply {
                                latitude = stabilizedLat
                                longitude = stabilizedLon
                                accuracy = loc.accuracy
                                time = loc.time
                            }

                    viewModelScope.launch {
                        _location.value = stabilizedLocation
                    }
                }
            }
        }
        locationCallback?.let{callback ->
            fusedLocationClient?.requestLocationUpdates(
                request,
                callback,
                null
            )
        }
    }
    fun stopLocationUpdates (){
        locationCallback?.let{
            fusedLocationClient?.removeLocationUpdates { it }
        }
        locationCallback = null
    }
    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
    }
    private fun lowPassFilter(currentValue: Double, alpha: Float, lastValue: Double): Double {
        return lastValue + alpha * (currentValue - lastValue)
    }
}