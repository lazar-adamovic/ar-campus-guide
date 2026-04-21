package com.example.projekat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.projekat.ar.ARSceneScreen
import com.example.projekat.location.LocationViewModel
import com.example.projekat.orientation.OrientationViewModel
import com.example.projekat.permissions.PermissionsManager
import com.example.projekat.ui.theme.ProjekatTheme


class MainActivity : ComponentActivity() {
    private val locationViewModel by viewModels<LocationViewModel>()
    private val orientationViewModel by viewModels<OrientationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjekatTheme {
                PermissionsManager{
                    ARSceneScreen(
                        locationViewModel = locationViewModel,
                        orientationViewModel = orientationViewModel
                    )
                }
            }
        }
    }
}