package com.example.wifisignalmapper.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifisignalmapper.ui.theme.SignalExcellent
import com.example.wifisignalmapper.ui.theme.SignalFair
import com.example.wifisignalmapper.ui.theme.SignalGood
import com.example.wifisignalmapper.ui.theme.SignalPoor
import com.example.wifisignalmapper.ui.theme.SignalWeak
import com.example.wifisignalmapper.viewmodels.SignalStats
import com.example.wifisignalmapper.viewmodels.WifiScanViewModel
import java.lang.StrictMath.abs
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun LocationSelectionScreen(
    viewModel: WifiScanViewModel,
    onNavigateToScan: (String) -> Unit,
    onNavigateToResult: (String) -> Unit
) {
    val scanData by viewModel.scanData.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (isDarkTheme) {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                        )
                                    }
                                )
                            )
                            .padding(vertical = 32.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Left side - Icon in a circular background
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = if (isDarkTheme) {
                                                listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                )
                                            } else {
                                                listOf(
                                                    Color.White.copy(alpha = 0.9f),
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                )
                                            }
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiFind,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            // Right side - Text content
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "WiFi Signal Mapper",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Map and analyze WiFi signal strength across your locations",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                
                // Locations list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.availableLocations) { location ->
                        val hasResults = scanData.locationResults.containsKey(location)
                        val stats = viewModel.getStatsForLocation(location)
                        
                        LocationCard(
                            location = location,
                            stats = stats,
                            onScan = { onNavigateToScan(location) },
                            onViewResults = { onNavigateToResult(location) },
                            hasResults = hasResults
                        )
                    }
                }
            }
            
            // Clear All button with improved styling
            if (scanData.locationResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    IconButton(
                        onClick = { viewModel.clearAllData() },
                        modifier = Modifier
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All Data",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    location: String,
    stats: SignalStats?,
    onScan: () -> Unit,
    onViewResults: () -> Unit,
    hasResults: Boolean
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Location header with improved styling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
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
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = location,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Signal Status Card with improved design
            if (stats != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Signal header with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalWifi4Bar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Text(
                                text = "Signal Overview",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Signal stats with improved layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stats details
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(getSignalColor(stats.avg), CircleShape)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                                    )
                                    Text(
                                        text = " Average: ${stats.avg} dBm",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Range: ${stats.min} to ${stats.max} dBm",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Network count with improved styling
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${stats.signalCount}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "networks",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Signal strength indicator bar
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDarkTheme) {
                                    Color.LightGray.copy(alpha = 0.5f)
                                } else {
                                    Color.LightGray.copy(alpha = 0.3f)
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
            
            // Action buttons with improved styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onScan,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalWifi4Bar,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (hasResults) "Rescan" else "Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (hasResults) {
                    Button(
                        onClick = onViewResults,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "View Map",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Signal color helper
private fun getSignalColor(level: Int): Color {
    return when {
        level >= -60 -> SignalExcellent // Strong signal (Green)
        level >= -70 -> SignalGood      // Good signal (Light Green)
        level >= -80 -> SignalFair      // Fair signal (Yellow)
        level >= -90 -> SignalPoor      // Poor signal (Orange)
        else -> SignalWeak              // Very poor signal (Red)
    }
}

// Calculate bar width for signal strength visualization
private fun calculateBarWidth(min: Int, max: Int): Float {
    // Convert negative dBm values to a percentage (0.0 to 1.0)
    // Typically, WiFi signals range from -30 dBm (excellent) to -100 dBm (terrible)
    val percentage = (abs(min) - 30).toFloat() / 70f
    
    // Ensure the percentage is between 0.1 and 1.0
    return (1f - percentage).coerceIn(0.1f, 1.0f)
} 