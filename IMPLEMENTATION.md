# Implementation and Architecture

## 1. Architecture Overview

The application follows the **MVVM** (Model-View-ViewModel) pattern with Jetpack Compose UI:

- **Model**: Data classes in `models/` represent the domain data.
- **ViewModel**: Business logic and data persistence in `WifiScanViewModel.kt`.
- **View**: Composable screens under `ui/screens/` for interface and interactions.
- **Theme**: Material 3 theming in `ui/theme/`.

---

## 2. Key Files and Responsibilities

### 2.1 MainActivity.kt
Handles app startup, permission checks, and navigation between screens.

```kotlin
// ... MainActivity logic ...
override fun onCreate(savedInstanceState: Bundle?) {
    setContent {
        WiFiSignalMapperTheme {
            // Navigation state
            when (currentScreen) {
                Screen.LocationSelection -> LocationSelectionScreen(...)
                Screen.Scanning -> ScanningScreen(...)
                Screen.Results -> ResultsScreen(...)
            }
        }
    }
}
```

### 2.2 ViewModel: WifiScanViewModel.kt
Manages scan state, collects WiFi signals, persists data with SharedPreferences.

```kotlin
// Start a scan and save results
fun startScan(location: String) {
    scanState = ScanState.SCANNING
    viewModelScope.launch(Dispatchers.IO) {
        val results = wifiManager.scanResults.map { WifiSignal(it.SSID, it.BSSID, it.level) }
        // Persist in SharedPreferences
        saveToPrefs(location, results)
        _scanData.update { it.copy(locationResults = it.locationResults + (location to LocationScanResult(results))) }
        scanState = ScanState.COMPLETED
    }
}

private fun saveToPrefs(location: String, list: List<WifiSignal>) { /* ... */ }
```

### 2.3 Data Models

#### WifiSignal.kt
```kotlin
// Represents one WiFi sample
data class WifiSignal(
    val ssid: String,
    val bssid: String,
    val level: Int
)
```

#### LocationScanResult.kt
```kotlin
// Holds all samples for a location
data class LocationScanResult(
    val scanResults: List<WifiSignal>
)
```

---

## 3. UI Components

All screens use Jetpack Compose in `app/src/main/java/com/example/wifisignalmapper/ui/screens`

### 3.1 LocationSelectionScreen.kt
Displays available locations with cards, scan/view actions, and clear-all button.

```kotlin
@Composable
fun LocationCard(...) {
    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
        Row { /* location name and icon */ }
        /* Signal statistics card */
        /* Buttons: Scan / View Map */
    }
}
```

### 3.2 ScanningScreen.kt
Shows a single pulsing WiFi icon wrapped by a circular loader.

```kotlin
@Composable
fun ScanningScreen(...) {
    Box { 
        // Loader container
        Box(modifier = Modifier.size(160.dp)) {
            CircularProgressIndicator(...)  
            Icon(Icons.Default.SignalWifi4Bar, modifier = Modifier.scale(pulse.value))
        }
        Text("Scanning WiFi Networks")
    }
}
```

### 3.3 ResultsScreen.kt
Tabbed layout (`TabRow`) with three tabs:

1. **Signal Matrix** (`SignalMatrixTab`) draws a 10×10 grid:
```kotlin
Canvas(modifier = Modifier.fillMaxSize()) {
    val squareSize = size.width / 10
    scanData.forEachIndexed { i, signal ->
        drawRect(color = getSignalColor(signal.level),
                 topLeft = Offset(x, y),
                 size = Size(squareSize, squareSize))
    }
}
```
2. **Networks** (`NetworkListTab`) shows a styled list of `WifiSignal` entries.
3. **Location Comparison** (`LocationComparisonTab`) compares statistics across locations.

Helper functions:
```kotlin
private fun getSignalColor(level: Int): Color = when {
  level >= -60 -> SignalExcellent
  level >= -70 -> SignalGood
  else -> SignalWeak
}

private fun calculateBarWidth(min: Int, max: Int): Float = /* ... */
```

---

## 4. Theming and Styling

Defined in `ui/theme/`

- **Color.kt**: Custom blue-teal palette for light/dark, plus signal strength colors.
- **Theme.kt**: Applies `darkColorScheme` and `lightColorScheme`, sets status bar color.
- **Typography.kt**: Font styles (default Material3 Typography). 

---

## 5. Usage Flow

1. **Open App** → `LocationSelectionScreen.kt`
2. **Select a Location** → `ScanningScreen.kt` (single loader)
3. **Wait for Scan** → `ResultsScreen.kt`
4. **View Tabs**: Matrix, Networks, Comparison
5. **Clear Data** via header icon
