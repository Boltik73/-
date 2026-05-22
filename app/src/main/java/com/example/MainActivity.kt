package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

// Data models
data class VpnServer(
    val name: String,
    val flag: String,
    val description: String,
    val basePing: Int,
    var currentPing: Int = basePing
)

data class TunnelMode(
    val name: String,
    val protocol: String,
    val description: String
)

data class OperatorPayload(
    val name: String,
    val operator: String,
    val region: String,
    val sni: String,
    val requestHeader: String
)

data class LogEntry(
    val timestamp: String,
    val tag: String,
    val message: String,
    val type: LogType
)

enum class LogType { INFO, SUCCESS, WARNING, ERROR }

@Composable
fun MainAppContainer() {
    var selectedTab by remember { mutableStateOf("Home") }
    
    // Core VPN states shared across screens
    var isConnected by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }
    var currentSpeedDl by remember { mutableStateOf(0.0) }
    var currentSpeedUl by remember { mutableStateOf(0.0) }
    var gamingPing by remember { mutableStateOf(14) }
    
    // Config selections
    var selectedServer by remember { 
        mutableStateOf(VpnServer("Ukraine – Kyiv (Game Optimized)", "🇺🇦", "Premium SSH Server", 12)) 
    }
    
    var selectedMode by remember { 
        mutableStateOf(TunnelMode("Direct SSL / TLS", "SSH-TLS", "Direct SSH tunnel protected by modern TLS encryption")) 
    }
    
    var selectedPayload by remember { 
        mutableStateOf(OperatorPayload("Kyivstar Free Cloud v2.4", "Kyivstar", "Ukraine", "freecloud.kyivstar.ua", "CONNECT [host_port] HTTP/1.1\\r\\nHost: freecloud.kyivstar.ua\\r\\n\\r\\n")) 
    }

    // Dynamic log buffer
    val logsList = remember { mutableStateListOf<LogEntry>() }
    val context = LocalContext.current
    
    // Format timestamp helper
    val timeFormat = SimpleNameHolder.timeFormat

    // Simulate logs when connecting
    LaunchedEffect(isConnecting) {
        if (isConnecting) {
            logsList.add(LogEntry(timeFormat.format(Date()), "CORE", "Initializing local tunnel interface...", LogType.INFO))
            delay(300)
            logsList.add(LogEntry(timeFormat.format(Date()), "SSH", "Establishing connection to ${selectedServer.name} via Port 443...", LogType.INFO))
            delay(400)
            logsList.add(LogEntry(timeFormat.format(Date()), "PAYLOAD", "Injecting headers for ${selectedPayload.operator} ...", LogType.INFO))
            logsList.add(LogEntry(timeFormat.format(Date()), "PAYLOAD", "SNI Host: ${selectedPayload.sni}", LogType.INFO))
            delay(400)
            logsList.add(LogEntry(timeFormat.format(Date()), "PROXY", "Tunnel Handshake with mode: ${selectedMode.name}", LogType.INFO))
            delay(300)
            logsList.add(LogEntry(timeFormat.format(Date()), "CRYPTO", "Encryption negotiated: AES-256-GCM / DH-2048 keys", LogType.INFO))
            delay(200)
            logsList.add(LogEntry(timeFormat.format(Date()), "CORE", "Tunnel established successfully on port 1080", LogType.SUCCESS))
            logsList.add(LogEntry(timeFormat.format(Date()), "CORE", "By-passing Ukrainian Operator restrictions automatically.", LogType.SUCCESS))
            isConnecting = false
            isConnected = true
            gamingPing = selectedServer.basePing + Random.nextInt(-2, 3)
        }
    }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            currentSpeedDl = 0.0
            currentSpeedUl = 0.0
        } else {
            // Background traffic fluctuation simulation
            while (isConnected) {
                delay(1200)
                currentSpeedDl = Random.nextDouble(1.5, 9.8)
                currentSpeedUl = Random.nextDouble(0.4, 3.2)
                if (Random.nextFloat() > 0.7f) {
                    gamingPing = selectedServer.currentPing + Random.nextInt(-2, 3)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F1115),
                tonalElevation = 12.dp,
                modifier = Modifier.border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                listOf(
                    Pair("Home", Icons.Default.Home),
                    Pair("Servers", Icons.Default.Place),
                    Pair("Logs", Icons.Default.List),
                    Pair("About", Icons.Default.Info)
                ).forEach { item ->
                    val isTabSelected = selectedTab == item.first
                    NavigationBarItem(
                        selected = isTabSelected,
                        onClick = { selectedTab = item.first },
                        icon = { 
                            Icon(
                                imageVector = item.second, 
                                contentDescription = item.first,
                                tint = if (isTabSelected) Color(0xFF6366F1) else Color(0xFF94A3B8)
                            ) 
                        },
                        label = { 
                            Text(
                                text = item.first, 
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isTabSelected) Color(0xFFF1F5F9) else Color(0xFF94A3B8)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF6366F1).copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFF0F1115)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabSwitcher"
            ) { tab ->
                when (tab) {
                    "Home" -> HomeScreen(
                        isConnected = isConnected,
                        isConnecting = isConnecting,
                        speedDl = currentSpeedDl,
                        speedUl = currentSpeedUl,
                        pingValue = gamingPing,
                        selectedServer = selectedServer,
                        selectedMode = selectedMode,
                        selectedPayload = selectedPayload,
                        onConnectClick = {
                            if (isConnected) {
                                isConnected = false
                                logsList.add(LogEntry(timeFormat.format(Date()), "CORE", "Slight connection termination triggered.", LogType.WARNING))
                                logsList.add(LogEntry(timeFormat.format(Date()), "CORE", "Tunnel stopped by user.", LogType.INFO))
                            } else if (!isConnecting) {
                                isConnecting = true
                            }
                        },
                        onChangeServer = { selectedServer = it },
                        onChangeMode = { selectedMode = it },
                        onChangePayload = { selectedPayload = it }
                    )
                    "Servers" -> ServersScreen(
                        selectedServer = selectedServer,
                        onServerSelect = { 
                            selectedServer = it 
                            selectedTab = "Home"
                            Toast.makeText(context, "Selected Server: ${it.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "Logs" -> LogsScreen(
                        logs = logsList,
                        onClearLogs = { logsList.clear() }
                    )
                    "About" -> AboutScreen()
                }
            }
        }
    }
}

// Global holder for state formatting to bypass lint/warning issues
object SimpleNameHolder {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
}

@Composable
fun HomeScreen(
    isConnected: Boolean,
    isConnecting: Boolean,
    speedDl: Double,
    speedUl: Double,
    pingValue: Int,
    selectedServer: VpnServer,
    selectedMode: TunnelMode,
    selectedPayload: OperatorPayload,
    onConnectClick: () -> Unit,
    onChangeServer: (VpnServer) -> Unit,
    onChangeMode: (TunnelMode) -> Unit,
    onChangePayload: (OperatorPayload) -> Unit
) {
    var showServerDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showPayloadDialog by remember { mutableStateOf(false) }

    // Pulsing animation for active connection glow
    val infiniteTransition = rememberInfiniteTransition(label = "PulseGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SizePulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UA-Tunnel Pro",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) Color(0xFF10B981) else if (isConnecting) Color(0xFFF59E0B) else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "Connected (Bypass UA Active)" else if (isConnecting) "Configuring Tunnel..." else "Disconnected (Secure Off)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isConnected) Color(0xFF10B981) else if (isConnecting) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            
            // Custom Status Tag
            Surface(
                color = Color(0xFF6366F1).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
            ) {
                Text(
                    text = "UKRAINE BYPASS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Connection Glow Circle Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
        ) {
            // Pulse Rings
            if (isConnected || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isConnected) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFF59E0B).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .scale(glowScale * 0.9f)
                        .border(
                            width = 1.dp,
                            color = if (isConnected) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFF59E0B).copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }

            // Central Interactive Button
            Surface(
                modifier = Modifier
                    .size(150.dp)
                    .clickable(onClick = onConnectClick),
                shape = CircleShape,
                color = Color(0xFF1E293B),
                border = BorderStroke(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = if (isConnected) {
                            listOf(Color(0xFF10B981), Color(0xFF34D399))
                        } else if (isConnecting) {
                            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
                        } else {
                            listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                        }
                    )
                ),
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Power",
                        modifier = Modifier.size(42.dp),
                        tint = if (isConnected) Color(0xFF10B981) else if (isConnecting) Color(0xFFF59E0B) else Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isConnected) "STOP TUNNEL" else if (isConnecting) "CONNECTING" else "START TUNNEL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) Color(0xFF10B981) else if (isConnecting) Color(0xFFF59E0B) else Color.White,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        // Live Speedometer Bandwidth Info Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DL Speed
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "DL", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DOWNLOAD", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f MB/s", speedDl),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Divider(
                    color = Color(0xFF334155),
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                )

                // UL Speed
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.scale(1f, -1f)) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "UL", tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("UPLOAD", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f MB/s", speedUl),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Divider(
                    color = Color(0xFF334155),
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                )

                // Ping indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Ping", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOW PING", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = if (isConnected) "${pingValue} ms" else "-- ms",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) Color(0xFF10B981) else Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Configuration Selection list Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Selector Server
            ConfigSelectionRow(
                icon = "🇺🇦",
                title = "VPN Server Profile",
                currentSelection = selectedServer.name,
                onClick = { showServerDialog = true }
            )

            // 2. Selector Mode
            ConfigSelectionRow(
                icon = "🛡️",
                title = "Tunneling Protocol",
                currentSelection = selectedMode.name,
                onClick = { showModeDialog = true }
            )

            // 3. Payload Config Selector
            ConfigSelectionRow(
                icon = "📡",
                title = "Ukraine Free Config / SNI",
                currentSelection = "${selectedPayload.operator} (${selectedPayload.name})",
                onClick = { showPayloadDialog = true }
            )
        }
    }

    // Server list Dialog selection
    if (showServerDialog) {
        SelectServerDialog(
            currentSelected = selectedServer,
            onDismiss = { showServerDialog = false },
            onSelect = {
                onChangeServer(it)
                showServerDialog = false
            }
        )
    }

    // Tunnel mode Dialog selection
    if (showModeDialog) {
        SelectModeDialog(
            currentSelected = selectedMode,
            onDismiss = { showModeDialog = false },
            onSelect = {
                onChangeMode(it)
                showModeDialog = false
            }
        )
    }

    // Operator payload Dialog selection
    if (showPayloadDialog) {
        SelectPayloadDialog(
            currentSelected = selectedPayload,
            onDismiss = { showPayloadDialog = false },
            onSelect = {
                onChangePayload(it)
                showPayloadDialog = false
            }
        )
    }
}

@Composable
fun ConfigSelectionRow(
    icon: String,
    title: String,
    currentSelection: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131720)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .wrapContentSize(Alignment.Center)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentSelection,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Edit Config",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Dialog: Select Server
@Composable
fun SelectServerDialog(
    currentSelected: VpnServer,
    onDismiss: () -> Unit,
    onSelect: (VpnServer) -> Unit
) {
    val servers = listOf(
        VpnServer("Ukraine – Kyiv (Game Optimized)", "🇺🇦", "Lowest ping, optimal for CS:GO, Pubg & Discord", 12),
        VpnServer("Ukraine – Lviv (Ultra Speed)", "🇺🇦", "High-capacity network backend port for video streams", 17),
        VpnServer("Ukraine – Odesa (High Speed)", "🇺🇦", "Best SSH tunnel direct proxy routing", 22),
        VpnServer("Ukraine – Kharkiv (Normal)", "🇺🇦", "Redundant proxy connection interface", 28),
        VpnServer("Germany – Frankfurt (Bypass Backup)", "🇩🇪", "Safe global fallback proxy location", 45)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select VPN Server Endpoint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(servers) { server ->
                        val isSelected = server.name == currentSelected.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF6366F1).copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { onSelect(server) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(server.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(server.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(server.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Text(
                                text = "${server.basePing} ms",
                                color = if (server.basePing < 20) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Select Mode
@Composable
fun SelectModeDialog(
    currentSelected: TunnelMode,
    onDismiss: () -> Unit,
    onSelect: (TunnelMode) -> Unit
) {
    val modes = listOf(
        TunnelMode("Direct SSH (No Proxy)", "SSH-DIRECT", "Raw SSH connection, connects perfectly when operator allows simple proxy"),
        TunnelMode("Direct SSL / TLS", "SSH-TLS", "Recommended for encryption. Deep packet inspection protection"),
        TunnelMode("HTTP Proxy Setup", "SSH-HTTP", "Allows customized payloads & headers to inject free gateway bypasses"),
        TunnelMode("SSL / TLS Proxy", "SSH-TLS-PROXY", "Combines secure HTTP payload injection and full SSL/TLS encryption")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Secure tunneling Protocol",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(modes) { mode ->
                        val isSelected = mode.name == currentSelected.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF6366F1).copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { onSelect(mode) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.protocol,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8),
                                        modifier = Modifier
                                            .background(Color(0xFF818CF8).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(mode.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(mode.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Select Payload
@Composable
fun SelectPayloadDialog(
    currentSelected: OperatorPayload,
    onDismiss: () -> Unit,
    onSelect: (OperatorPayload) -> Unit
) {
    val payloads = listOf(
        OperatorPayload("Kyivstar Free Cloud v2.4", "Kyivstar", "UA", "freecloud.kyivstar.ua", "CONNECT [host_port] HTTP/1.1\\r\\nHost: freecloud.kyivstar.ua\\r\\n\\r\\n"),
        OperatorPayload("Kyivstar Web Bypass Pro", "Kyivstar", "UA", "mpdg.kyivstar.ua", "CONNECT [host_port] HTTP/2.0\\r\\nHost: mpdg.kyivstar.ua\\r\\nConnection: Keep-Alive\\r\\n"),
        OperatorPayload("Lifecell Free Social Proxy", "Lifecell", "UA", "lifecell.ua", "GET / HTTP/1.1\\r\\nHost: lifecell.ua\\r\\n"),
        OperatorPayload("Lifecell Music SNI Stream", "Lifecell", "UA", "music.lifecell.ua", "CONNECT [host_port] HTTP/1.1\\r\\nHost: music.lifecell.ua\\r\\n\\r\\n"),
        OperatorPayload("Vodafone UA Online Access", "Vodafone UA", "UA", "vodafone.ua", "CONNECT [host_port] HTTP/1.1\\r\\nHost: vodafone.ua\\r\\n"),
        OperatorPayload("Vodafone Gaming Host Bypass", "Vodafone UA", "UA", "games.vodafone.ua", "CONNECT [host_port] HTTP/1.1\\r\\nHost: games.vodafone.ua\\n")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Ukraine Operator Payload",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(payloads) { payload ->
                        val isSelected = payload.name == currentSelected.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF6366F1).copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { onSelect(payload) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val opColor = when (payload.operator) {
                                        "Kyivstar" -> Color(0xFF3B82F6)
                                        "Lifecell" -> Color(0xFFFBBF24)
                                        else -> Color(0xFFEF4444)
                                    }
                                    Text(
                                        text = payload.operator,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = opColor,
                                        modifier = Modifier
                                            .background(opColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(payload.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("SNI: ${payload.sni}", color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Servers detail list screen
@Composable
fun ServersScreen(
    selectedServer: VpnServer,
    onServerSelect: (VpnServer) -> Unit
) {
    val serversList = remember {
        mutableStateListOf(
            VpnServer("Ukraine – Kyiv (Game Optimized)", "🇺🇦", "Dedicated high speed game route", 12),
            VpnServer("Ukraine – Lviv (Ultra Speed)", "🇺🇦", "Full gigabit bypass backhaul port", 17),
            VpnServer("Ukraine – Odesa (High Speed)", "🇺🇦", "Southern region low latency route", 22),
            VpnServer("Ukraine – Kharkiv (Normal)", "🇺🇦", "Direct SSH route via border transit", 28),
            VpnServer("Germany – Frankfurt (Bypass Backup)", "🇩🇪", "Full bypass failover route", 45)
        )
    }

    var isReevaluating by remember { mutableStateOf(false) }

    // Run ping measurement simulation
    LaunchedEffect(isReevaluating) {
        if (isReevaluating) {
            delay(1000)
            serversList.forEachIndexed { idx, srv ->
                serversList[idx] = srv.copy(currentPing = srv.basePing + Random.nextInt(-3, 3))
            }
            isReevaluating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "High-Speed Servers",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Bypasses for all Ukrainian Operators",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
            
            // Re-evaluate button
            IconButton(
                onClick = { isReevaluating = true },
                modifier = Modifier
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
            ) {
                if (isReevaluating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF6366F1))
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Test Ping", tint = Color.White)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(serversList) { server ->
                val isSelected = server.name == selectedServer.name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServerSelect(server) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.08f) else Color(0xFF131720)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF6366F1) else Color(0xFF1E293B)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = server.flag,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .wrapContentSize(Alignment.Center)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = server.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = server.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${server.currentPing} ms",
                                color = if (server.currentPing < 20) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (server.currentPing < 25) "EXCELLENT" else "GOOD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (server.currentPing < 25) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Detailed logs screen
@Composable
fun LogsScreen(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Tunnel Logs",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "SSH connection logs & response code metrics",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Copy Logs
                IconButton(
                    onClick = {
                        val allLogsText = logs.joinToString("\n") { "[${it.timestamp}] ${it.tag}: ${it.message}" }
                        if (allLogsText.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(allLogsText))
                            Toast.makeText(context, "Logs Copied to Clipboard", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No Logs to Copy", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFF1E293B), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Copy logs", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                // Clear Logs
                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier
                        .background(Color(0xFF1E293B), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear logs", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
            color = Color(0xFF0B0D11),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (logs.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Empty", tint = Color(0xFF334155), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Connection Logs recorded yet.", color = Color(0xFF475569), fontSize = 12.sp)
                        Text("Start the VPN tunnel to show live stream logs.", color = Color(0xFF334155), fontSize = 10.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs) { log ->
                        val textColor = when (log.type) {
                            LogType.INFO -> Color(0xFF94A3B8)
                            LogType.SUCCESS -> Color(0xFF10B981)
                            LogType.WARNING -> Color(0xFFF59E0B)
                            LogType.ERROR -> Color(0xFFEF4444)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "[${log.timestamp}]",
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "${log.tag}:",
                                color = Color(0xFF818CF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(68.dp)
                            )
                            Text(
                                text = log.message,
                                color = textColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// About operator payloads, tips & disclaimer screen
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Bypasses & Payloads",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Read about Ukraine Operator Internet configurations",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AboutCard(
                    title = "What is free internet payload bypass?",
                    desc = "Some Operators inside Ukraine allow access to their base educational/billing cloud services or social network packs without active payment. UA-Tunnel Pro injects a specialized HTTP/SNI payload proxy wrapper to direct external SSH connections inside these cloud gates.",
                    color = Color(0xFF6366F1),
                    icon = Icons.Default.Info
                )
            }
            item {
                AboutCard(
                    title = "Gaming Ping Tweaking",
                    desc = "The built-in Game Optimization profile routes internal game packets with priority, lowering the ping rate for online titles like Wild Rift, PUBG UA, Counter-Strike, and Telegram calls. Enable Direct SSL Mode for best gaming results.",
                    color = Color(0xFF10B981),
                    icon = Icons.Default.Favorite
                )
            }
            item {
                AboutCard(
                    title = "Legal notice and Disclaimer",
                    desc = "Connection utilities are offered strictly for network research and security audits. Use the Ukrainian operator settings exclusively as backup educational communication portals.",
                    color = Color(0xFFEF4444),
                    icon = Icons.Default.Warning
                )
            }
        }
    }
}

@Composable
fun AboutCard(
    title: String,
    desc: String,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131720)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = desc,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
