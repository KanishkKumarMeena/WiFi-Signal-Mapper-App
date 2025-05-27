package com.example.wifisignalmapper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wifisignalmapper.ui.components.PermissionHandler
import com.example.wifisignalmapper.ui.screens.LocationSelectionScreen
import com.example.wifisignalmapper.ui.screens.ResultsScreen
import com.example.wifisignalmapper.ui.screens.ScanningScreen
import com.example.wifisignalmapper.ui.theme.WiFiSignalMapperTheme
import com.example.wifisignalmapper.viewmodels.WifiScanViewModel

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate called - initializing app")
        
        setContent {
            WiFiSignalMapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create ViewModel at the top level to ensure it survives configuration changes
                    val viewModel: WifiScanViewModel = viewModel()
                    
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.LocationSelection) }
                    var selectedLocation by remember { mutableStateOf<String?>(null) }
                    
                    PermissionHandler(
                        onPermissionsGranted = {
                            // When permissions are granted, ensure data is loaded
                            Log.d(TAG, "Permissions granted, data should be loaded")
                        }
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.LocationSelection -> {
                                LocationSelectionScreen(
                                    viewModel = viewModel,
                                    onNavigateToScan = { location ->
                                        selectedLocation = location
                                        currentScreen = Screen.Scanning
                                    },
                                    onNavigateToResult = { location ->
                                        selectedLocation = location
                                        currentScreen = Screen.Results
                                    }
                                )
                            }
                            is Screen.Scanning -> {
                                selectedLocation?.let { location ->
                                    ScanningScreen(
                                        viewModel = viewModel,
                                        locationName = location,
                                        onScanComplete = {
                                            currentScreen = Screen.Results
                                        },
                                        onBackClick = {
                                            currentScreen = Screen.LocationSelection
                                        }
                                    )
                                }
                            }
                            is Screen.Results -> {
                                selectedLocation?.let { location ->
                                    ResultsScreen(
                                        viewModel = viewModel,
                                        locationName = location,
                                        onBackClick = {
                                            currentScreen = Screen.LocationSelection
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called - app coming to foreground")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called - app going to background")
    }
}

sealed class Screen {
    object LocationSelection : Screen()
    object Scanning : Screen()
    object Results : Screen()
}