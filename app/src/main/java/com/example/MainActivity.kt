package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { VpnBottomNavigationBar() }
        ) { innerPadding ->
          VpnManagerScreen(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun VpnBottomNavigationBar() {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        listOf("Home", "Servers", "Logs", "About").forEach { item ->
            NavigationBarItem(
                selected = item == "Home",
                onClick = {},
                icon = { Icon(Icons.Filled.Home, contentDescription = item) },
                label = { Text(item) }
            )
        }
    }
}

@Composable
fun VpnManagerScreen(modifier: Modifier = Modifier) {
    var connectionMode by remember { mutableStateOf("Direct SSL/TLS") }
    var isConnected by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "UA-Tunnel Pro", style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurface))
                    Text(text = "System Ready", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                }
            }

            // Connection Controller
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                // Outer Glow simulated
                Surface(modifier = Modifier.size(200.dp), shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {}
                Button(
                    modifier = Modifier.size(160.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { isConnected = !isConnected }
                ) {
                    Text(if (isConnected) "Disconnect" else "Connect")
                }
            }
            
            // Stats Row
            Row(modifier = Modifier.padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gaming Ping", style = MaterialTheme.typography.labelSmall)
                    Text("24ms", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Encryption", style = MaterialTheme.typography.labelSmall)
                    Text("AES-256", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary))
                }
            }

            // Configuration Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Server Selector
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🇺🇦", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Location", style = MaterialTheme.typography.labelSmall)
                            Text("Ukraine — Kyiv (Game Optimized)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Tunneling Protocol Selector
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                             Text("Tunnel Mode", style = MaterialTheme.typography.labelSmall)
                             Text(connectionMode, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                // Payload Selector
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📡", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                             Text("Payload / Configuration", style = MaterialTheme.typography.labelSmall)
                             Text("Kyivstar Free Cloud v2.4", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

