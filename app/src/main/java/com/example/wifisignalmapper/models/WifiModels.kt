package com.example.wifisignalmapper.models

import android.net.wifi.ScanResult

/**
 * Represents a single WiFi scan result with SSID and signal strength
 */
data class WifiSignal(
    val ssid: String,
    val bssid: String,
    val level: Int, // Signal strength in dBm
    val frequency: Int // Frequency in MHz
)

/**
 * Represents a set of 100 scan results at a specific location
 */
data class LocationScanResult(
    val locationName: String,
    val scanResults: List<WifiSignal>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Container for scan results from different locations
 */
data class WifiScanData(
    val locationResults: Map<String, LocationScanResult> = mapOf()
)

/**
 * Maps Android ScanResult to our simplified WifiSignal model
 */
fun ScanResult.toWifiSignal(): WifiSignal {
    return WifiSignal(
        ssid = if (SSID.isNullOrBlank()) "<Hidden>" else SSID,
        bssid = BSSID,
        level = level,
        frequency = frequency
    )
} 