package com.example.wifisignalmapper.viewmodels

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifisignalmapper.models.LocationScanResult
import com.example.wifisignalmapper.models.WifiScanData
import com.example.wifisignalmapper.models.WifiSignal
import com.example.wifisignalmapper.utils.DataStorage
import com.example.wifisignalmapper.utils.WifiScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for WiFi signal mapping
 */
class WifiScanViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val wifiScanner = WifiScanner(context)
    private val dataStorage = DataStorage(context)
    
    companion object {
        private const val TAG = "WifiScanViewModel"
    }

    // Predefined locations
    val availableLocations = listOf(
        "Location 1",
        "Location 2",
        "Location 3"
    )

    // UI States
    var scanState by mutableStateOf(ScanState.IDLE)
        private set
    
    var currentLocation by mutableStateOf<String?>(null)
        private set
    
    var lastScanResults by mutableStateOf<List<WifiSignal>>(emptyList())
        private set
    
    // Flow of scan data across locations
    private val _scanData = MutableStateFlow(WifiScanData())
    val scanData: StateFlow<WifiScanData> = _scanData.asStateFlow()
    
    init {
        // Load saved data when ViewModel is created
        Log.d(TAG, "Initializing ViewModel and loading saved data")
        loadSavedData()
    }
    
    /**
     * Load saved WiFi scan data from storage
     */
    fun loadSavedData() {
        try {
            Log.d(TAG, "Loading saved WiFi scan data from storage")
            val savedData = dataStorage.loadWifiScanData()
            Log.d(TAG, "Loaded data with ${savedData.locationResults.size} locations")
            for (location in savedData.locationResults.keys) {
                Log.d(TAG, "Found location: $location")
            }
            _scanData.value = savedData
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved data", e)
        }
    }
    
    /**
     * Check if required permissions are granted
     */
    fun hasRequiredPermissions(): Boolean {
        val hasWifiPermissions = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED && 
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CHANGE_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return hasWifiPermissions && hasLocationPermission
    }
    
    /**
     * Start scanning for WiFi signals at the given location
     */
    fun startScan(locationName: String) {
        if (!hasRequiredPermissions()) {
            scanState = ScanState.ERROR
            return
        }
        
        viewModelScope.launch {
            try {
                scanState = ScanState.SCANNING
                currentLocation = locationName
                
                // Get scan results
                val results = wifiScanner.startScan()
                    .catch { emit(emptyList()) }
                    .first()
                
                lastScanResults = results
                
                // Create location scan with 100 elements
                val locationScan = wifiScanner.collectLocationScan(locationName, results)
                
                // Update scan data
                val updatedLocations = _scanData.value.locationResults.toMutableMap()
                updatedLocations[locationName] = locationScan
                val updatedData = WifiScanData(updatedLocations)
                _scanData.value = updatedData
                
                // Save to persistent storage
                Log.d(TAG, "Saving scan data for location: $locationName with ${results.size} networks")
                dataStorage.saveLocationScan(locationName, locationScan)
                
                // Verify data was saved
                val verifyData = dataStorage.loadWifiScanData()
                Log.d(TAG, "Verification after save: ${verifyData.locationResults.size} locations in storage")
                
                scanState = ScanState.COMPLETED
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning WiFi", e)
                scanState = ScanState.ERROR
            }
        }
    }
    
    /**
     * Get statistics for a specific location
     */
    fun getStatsForLocation(locationName: String): SignalStats? {
        val locationData = _scanData.value.locationResults[locationName] ?: return null
        
        val levels = locationData.scanResults.map { it.level }
        
        return SignalStats(
            min = levels.minOrNull() ?: 0,
            max = levels.maxOrNull() ?: 0,
            avg = levels.average().toInt(),
            signalCount = locationData.scanResults.count { !it.ssid.startsWith("Padding-") },
            totalSignals = locationData.scanResults.size
        )
    }
    
    /**
     * Clear scan data for a location
     */
    fun clearLocation(locationName: String) {
        val updatedLocations = _scanData.value.locationResults.toMutableMap()
        updatedLocations.remove(locationName)
        _scanData.value = WifiScanData(updatedLocations)
        
        // Also clear from persistent storage
        Log.d(TAG, "Clearing data for location: $locationName")
        dataStorage.clearLocationData(locationName)
    }
    
    /**
     * Clear all saved data
     */
    fun clearAllData() {
        Log.d(TAG, "Clearing all saved data")
        _scanData.value = WifiScanData()
        dataStorage.clearAllData()
    }
    
    /**
     * Reset scanning state
     */
    fun resetScanState() {
        scanState = ScanState.IDLE
        currentLocation = null
    }
}

/**
 * Stats about signal strength
 */
data class SignalStats(
    val min: Int,
    val max: Int,
    val avg: Int,
    val signalCount: Int,
    val totalSignals: Int
)

/**
 * Scanning state
 */
enum class ScanState {
    IDLE,
    SCANNING,
    COMPLETED,
    ERROR
} 