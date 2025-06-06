# WiFi Signal Mapper

This Android app lets you scan and map WiFi signal strength across multiple locations. You can:
- Select a predefined location
- Perform a scan of 100 WiFi signal samples
- View the results in a colored matrix, detailed list, and location comparison

> [APK](https://github.com/KanishkKumarMeena/WiFi-Signal-Mapper-App/blob/96f4d74e90ae7ab5ff60ea0247ed51f85b270232/WiFi%20Signal%20Mapper.apk)

## App Screenshots
| Home Screen | Search Screen | Tracker Screen | Statistics Screen |
|-|-|-|-|
| ![Home](/Screenshots/Home.png) | ![Home2](/Screenshots/Home2.png) |![Matrix](/Screenshots/Matrix.png)| ![Networks](/Screenshots/Networks.png)|
![Scan](/Screenshots/Scan.gif) |

## Implementation Details

1. **Creation of App Interface**
   - MainActivity hosts the Compose screens: `LocationSelectionScreen.kt`, `ScanningScreen.kt`, `ResultsScreen.kt`.
   - Custom UI components defined in:
     - `LocationSelectionScreen.kt` (location cards, header card)
     - `ScanningScreen.kt` (animated loader)
     - `ResultsScreen.kt` (tab layout, matrix, lists, comparison cards)

2. **Ability to Log the Data**
   - `WifiScanViewModel.kt` logs scan results using `SharedPreferences`.
   - Scan results persisted per location in `LocationScanResult.kt` and `WifiSignal.kt`.
   - Data loaded on app startup and after each scan.

3. **Showing the Data in the Demo from Three Locations**
   - Predefined `viewModel.availableLocations` contains at least three locations.
   - After scanning, results are displayed for each location under "Results".
   - Comparison across locations in `LocationComparisonTab`.

---

## Workflow

1. **Launch App**
   - `MainActivity.kt` sets up the theme and navigation.

2. **Select a Location**
   - `LocationSelectionScreen.kt`: Tap one of the available locations.

3. **Perform Scan**
   - `ScanningScreen.kt`: Shows a single pulse loader wrapped by a circular progress bar.
   - When complete, transitions to Results screen.

4. **View Results**
   - `ResultsScreen.kt`: Three tabs:
     - **Signal Matrix**: `SignalMatrixTab` draws a 10×10 grid with color-coded strength.
     - **Networks**: `NetworkListTab` lists SSIDs, BSSIDs, levels, and summary stats.
     - **Location Comparison**: `LocationComparisonTab` compares stats across all scanned locations.

5. **Clear Data**
   - On home screen header, tap the delete icon to clear all stored scans.

---

## Implementation Highlights

- **MVVM Architecture** using `WifiScanViewModel.kt`.
- **Models**:
  - `WifiSignal.kt`: data class for SSID, BSSID, level.
  - `LocationScanResult.kt`: container for `List<WifiSignal>`.
- **Persistence**: `SharedPreferences` read/write in viewModel.
- **Compose UI**: organized in `ui/screens` with multiple composable functions.
- **Theming**: custom Material 3 theme in `ui/theme/Color.kt` and `Theme.kt`.

---

For more detailed implementation and architecture, see `IMPLEMENTATION.md`. 
