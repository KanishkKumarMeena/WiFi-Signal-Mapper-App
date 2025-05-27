package com.example.wifisignalmapper.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.wifisignalmapper.models.LocationScanResult
import com.example.wifisignalmapper.models.WifiScanData
import com.example.wifisignalmapper.models.WifiSignal
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Utility class for saving and loading WiFi scan data
 */
class DataStorage(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    // Custom Gson instance with pretty printing for debugging
    private val gson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .create()
    }
    
    /**
     * Save WiFi scan data to SharedPreferences
     */
    fun saveWifiScanData(wifiScanData: WifiScanData) {
        try {
            val mapType: Type = object : TypeToken<Map<String, LocationScanResult>>() {}.type
            val json = gson.toJson(wifiScanData.locationResults, mapType)
            
            // Log what we're storing for debugging purposes
            Log.d(TAG, "Saving data: $json")
            
            sharedPreferences.edit()
                .putString(KEY_WIFI_SCAN_DATA, json)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving WiFi scan data", e)
        }
    }
    
    /**
     * Load WiFi scan data from SharedPreferences
     */
    fun loadWifiScanData(): WifiScanData {
        try {
            val json = sharedPreferences.getString(KEY_WIFI_SCAN_DATA, null)
            Log.d(TAG, "Loading data: $json")
            
            if (json != null && json.isNotEmpty()) {
                val mapType: Type = object : TypeToken<Map<String, LocationScanResult>>() {}.type
                val locationResults: Map<String, LocationScanResult> = gson.fromJson(json, mapType)
                return WifiScanData(locationResults)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading WiFi scan data", e)
        }
        
        return WifiScanData()
    }
    
    /**
     * Save a location scan result
     */
    fun saveLocationScan(locationName: String, locationScanResult: LocationScanResult) {
        try {
            val currentData = loadWifiScanData()
            val updatedLocations = currentData.locationResults.toMutableMap()
            updatedLocations[locationName] = locationScanResult
            saveWifiScanData(WifiScanData(updatedLocations))
            
            // Verify data was properly stored by immediately reading it back
            val verifyData = loadWifiScanData()
            Log.d(TAG, "Verification: Data has ${verifyData.locationResults.size} locations")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving location scan", e)
        }
    }
    
    /**
     * Clear all saved scan data
     */
    fun clearAllData() {
        try {
            sharedPreferences.edit()
                .remove(KEY_WIFI_SCAN_DATA)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data", e)
        }
    }
    
    /**
     * Clear data for a specific location
     */
    fun clearLocationData(locationName: String) {
        try {
            val currentData = loadWifiScanData()
            val updatedLocations = currentData.locationResults.toMutableMap()
            updatedLocations.remove(locationName)
            saveWifiScanData(WifiScanData(updatedLocations))
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing location data", e)
        }
    }
    
    companion object {
        private const val PREF_NAME = "wifi_signal_mapper_prefs"
        private const val KEY_WIFI_SCAN_DATA = "wifi_scan_data"
        private const val TAG = "DataStorage"
    }
} 