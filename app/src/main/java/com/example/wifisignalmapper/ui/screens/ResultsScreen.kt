package com.example.wifisignalmapper.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.wifisignalmapper.models.LocationScanResult
import com.example.wifisignalmapper.models.WifiSignal
import com.example.wifisignalmapper.ui.theme.SignalExcellent
import com.example.wifisignalmapper.ui.theme.SignalFair
import com.example.wifisignalmapper.ui.theme.SignalGood
import com.example.wifisignalmapper.ui.theme.SignalPoor
import com.example.wifisignalmapper.ui.theme.SignalWeak
import com.example.wifisignalmapper.viewmodels.SignalStats
import com.example.wifisignalmapper.viewmodels.WifiScanViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: WifiScanViewModel,
    locationName: String,
    onBackClick: () -> Unit
) {
    val scanData by viewModel.scanData.collectAsState()
    val locationData = scanData.locationResults[locationName]
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Signal Matrix", "Networks", "Location Comparison")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Results: $locationName",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.shadow(8.dp)
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (locationData == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalWifi4Bar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No scan data available for this location",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                        }
                    }
                } else {
                    val stats = viewModel.getStatsForLocation(locationName)
                    
                    // Custom styled tab row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = { 
                            Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.primaryContainer) 
                        },
                        indicator = { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .height(3.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { 
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    when (selectedTab) {
                        0 -> SignalMatrixTab(locationData, stats)
                        1 -> NetworkListTab(locationData, stats)
                        2 -> LocationComparisonTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkListTab(locationData: LocationScanResult, stats: SignalStats?) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (stats != null) {
            // Enhanced stats card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Signal Statistics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left column - Signal details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(getSignalColor(stats.min), shape = RoundedCornerShape(2.dp))
                                        .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = "  Min: ${stats.min} dBm",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(getSignalColor(stats.max), shape = RoundedCornerShape(2.dp))
                                        .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = "  Max: ${stats.max} dBm",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Right column - Average and count
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Average: ${stats.avg} dBm",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Networks: ${stats.signalCount}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // Signal strength bar
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDarkTheme) {
                                Color.LightGray.copy(alpha = 0.3f)
                            } else {
                                Color.LightGray.copy(alpha = 0.2f)
                            })
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(calculateBarWidth(stats.min, stats.max))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(getSignalColor(stats.avg))
                        )
                    }
                }
            }
        }
        
        // Networks list card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Detected Networks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(locationData.scanResults.filter { !it.ssid.startsWith("Padding-") }) { signal ->
                        NetworkItem(signal, isDarkTheme)
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkItem(signal: WifiSignal, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal strength indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(getSignalColor(signal.level), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getSignalBars(signal.level).toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = signal.ssid,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BSSID: ${signal.bssid}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = "${signal.level} dBm",
                fontWeight = FontWeight.Bold,
                color = getSignalColor(signal.level)
            )
        }
    }
}

@Composable
fun SignalMatrixTab(locationData: LocationScanResult, stats: SignalStats?) {
    val signalData = locationData.scanResults
    val isDarkTheme = isSystemInDarkTheme()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card with improved styling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WiFi Signal Matrix",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "100 data points collected at this location",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                if (stats != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Signal quality indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Average signal with color indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(getSignalColor(stats.avg), shape = RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "Avg: ${stats.avg} dBm",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        // Min signal
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(getSignalColor(stats.min), shape = RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "Min: ${stats.min} dBm",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        // Max signal
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(getSignalColor(stats.max), shape = RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "Max: ${stats.max} dBm",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Signal matrix visualization card with improved styling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkTheme) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    })
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val squareSize = minOf(width / 10, height / 10)
                    val gridOffsetX = (width - (squareSize * 10)) / 2
                    val gridOffsetY = (height - (squareSize * 10)) / 2
                    
                    // Draw matrix cells
                    signalData.forEachIndexed { index, signal ->
                        val row = index / 10
                        val col = index % 10
                        
                        val x = gridOffsetX + (col * squareSize)
                        val y = gridOffsetY + (row * squareSize)
                        
                        // Cell background
                        drawRect(
                            color = getSignalColor(signal.level),
                            topLeft = Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                        )
                        
                        // Cell border
                        drawRect(
                            color = Color.Black.copy(alpha = 0.2f),
                            topLeft = Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                        )
                    }
                }
            }
        }
        
        // Legend card with improved styling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Signal Strength Legend",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Use Column instead of Row to avoid overflow on smaller screens
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // First row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem("Excellent (-60+)", getSignalColor(-50))
                        LegendItem("Good (-70)", getSignalColor(-70))
                    }
                    
                    // Second row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem("Fair (-80)", getSignalColor(-80))
                        LegendItem("Poor (-90)", getSignalColor(-90))
                    }
                    
                    // Third row centered
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LegendItem("Very Weak (< -90)", getSignalColor(-100))
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LocationComparisonTab(viewModel: WifiScanViewModel) {
    val scanData by viewModel.scanData.collectAsState()
    val locations = scanData.locationResults.keys.toList()
    val isDarkTheme = isSystemInDarkTheme()
    
    if (locations.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SignalWifi4Bar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Scan at least two locations to compare data",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Location Signal Comparison",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Compare WiFi signal strength across different locations",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // Create stats for each location
        val locationStats = locations.mapNotNull { location ->
            val stats = viewModel.getStatsForLocation(location)
            if (stats != null) Pair(location, stats) else null
        }
        
        // Location comparison cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(locationStats) { (location, stats) ->
                ComparisonCard(location, stats, isDarkTheme)
            }
        }
    }
}

@Composable
fun ComparisonCard(location: String, stats: SignalStats, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Location header with signal icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = location,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Signal stats in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SignalIndicator("Min", stats.min, getSignalColor(stats.min))
                SignalIndicator("Avg", stats.avg, getSignalColor(stats.avg))
                SignalIndicator("Max", stats.max, getSignalColor(stats.max))
                Text(
                    text = "${stats.signalCount} networks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Signal strength bar with improved styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkTheme) {
                        Color.LightGray.copy(alpha = 0.2f)
                    } else {
                        Color.LightGray.copy(alpha = 0.15f)
                    })
                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(12.dp))
            ) {
                // Signal bar with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth(calculateBarWidth(stats.min, stats.max))
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    getSignalColor(stats.min),
                                    getSignalColor(stats.avg),
                                    getSignalColor(stats.max)
                                )
                            )
                        )
                )
                
                // Signal text
                Text(
                    text = "Range: ${stats.min} to ${stats.max} dBm (Avg: ${stats.avg} dBm)",
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SignalIndicator(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(top = 4.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// Helper functions
private fun getSignalColor(level: Int): Color {
    return when {
        level >= -60 -> SignalExcellent // Strong signal (Green)
        level >= -70 -> SignalGood      // Good signal (Light Green)
        level >= -80 -> SignalFair      // Fair signal (Yellow)
        level >= -90 -> SignalPoor      // Poor signal (Orange)
        else -> SignalWeak              // Very poor signal (Red)
    }
}

private fun getSignalBars(level: Int): Int {
    return when {
        level >= -60 -> 4
        level >= -70 -> 3
        level >= -80 -> 2
        level >= -90 -> 1
        else -> 0
    }
}

private fun calculateBarWidth(min: Int, max: Int): Float {
    // Convert negative dBm values to a percentage (0.0 to 1.0)
    // Typically, WiFi signals range from -30 dBm (excellent) to -100 dBm (terrible)
    val signalRange = abs(min - max).coerceAtLeast(1)
    val percentage = (abs(min) - 30).toFloat() / 70f
    
    // Ensure the percentage is between 0.1 and 1.0
    return (1f - percentage).coerceIn(0.1f, 1.0f)
} 