package com.example.wifisignalmapper.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.example.wifisignalmapper.models.LocationScanResult
import com.example.wifisignalmapper.models.WifiSignal
import com.example.wifisignalmapper.models.toWifiSignal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

/**
 * Utility class to scan for WiFi signals
 */
class WifiScanner(private val context: Context) {
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    private fun hasRequiredPermissions(): Boolean {
        val hasWifiPermissions = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED && 
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CHANGE_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return hasWifiPermissions && hasLocationPermission
    }
    
    // Callback flow to collect scan results as a stream
    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    fun startScan(): Flow<List<WifiSignal>> = callbackFlow {
        if (!hasRequiredPermissions()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        // Receiver for scan results
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    try {
                        val results = wifiManager.scanResults
                            .map { it.toWifiSignal() }
                            .distinctBy { it.bssid }
                        trySend(results)
                    } catch (e: SecurityException) {
                        trySend(emptyList())
                    }
                } else {
                    trySend(emptyList())
                }
            }
        }
        
        // Register receiver
        context.registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        
        // Start scan
        try {
            val success = wifiManager.startScan()
            if (!success) {
                trySend(emptyList())
            }
        } catch (e: SecurityException) {
            trySend(emptyList())
        }
        
        // Clean up when flow collection ends
        awaitClose {
            try {
                context.unregisterReceiver(wifiScanReceiver)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
    }
    
    /**
     * Gets a snapshot of scan results for a specific location
     * Collects 100 elements for the scan matrix
     */
    suspend fun collectLocationScan(locationName: String, scanResults: List<WifiSignal>): LocationScanResult {
        // Create a matrix of 100 elements by either:
        // 1. Taking the top 100 strongest signals
        // 2. Padding with empty signals if less than 100
        // 3. Taking multiple readings if needed
        
        val processedResults = if (scanResults.size >= 100) {
            // Take top 100 strongest signals (least negative dBm values)
            scanResults.sortedByDescending { it.level }.take(100)
        } else {
            // Pad with dummy signals if less than 100
            val paddedList = scanResults.toMutableList()
            val padding = 100 - scanResults.size
            
            for (i in 1..padding) {
                paddedList.add(
                    WifiSignal(
                        ssid = "Padding-$i",
                        bssid = "00:00:00:00:00:00",
                        level = -100, // Very weak signal
                        frequency = 0
                    )
                )
            }
            paddedList
        }
        
        return LocationScanResult(
            locationName = locationName,
            scanResults = processedResults
        )
    }
} 