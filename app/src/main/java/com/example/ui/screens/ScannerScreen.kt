package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.camera.CameraPreviewComposable
import com.example.data.model.SwbStations
import com.example.data.model.SwbTrips
import com.example.data.model.TicketStatus
import com.example.ui.components.ScannerOverlay
import com.example.ui.theme.SwbCardBlue
import com.example.ui.theme.SwbCyan
import com.example.ui.theme.SwbDeepBlue
import com.example.ui.theme.SwbNavy
import com.example.ui.theme.SwbSurfaceBlue
import com.example.ui.theme.SwbValidGreen
import com.example.ui.theme.SwbYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ScannerViewModel

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val isContinuousMode by viewModel.isContinuousMode.collectAsState()
    val zoomRatio by viewModel.zoomRatio.collectAsState()
    val isFlashingBorder by viewModel.isFlashingBorder.collectAsState()
    val flashColor by viewModel.flashColor.collectAsState()
    val lastScanEvent by viewModel.lastScanEvent.collectAsState()
    val showResultCard by viewModel.showResultCard.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val currentTrip by viewModel.currentTrip.collectAsState()
    val tickets by viewModel.allTickets.collectAsState()

    val checkedCount = tickets.count { it.status == TicketStatus.CHECKED_IN.name }
    val totalCount = tickets.size

    var showStationMenu by remember { mutableStateOf(false) }
    var showTripMenu by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var showQuickTestDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SwbNavy)
    ) {
        // 1. Camera View or Fallback Background
        if (hasCameraPermission) {
            CameraPreviewComposable(
                isTorchOn = isTorchOn,
                zoomRatio = zoomRatio,
                onQrDetected = { code ->
                    viewModel.onQrScanned(code)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission request placeholder with Saigon Waterbus backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SwbNavy),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = SwbYellow,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cần quyền truy cập máy ảnh",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Để quét mã QR vé in nhiệt và vé điện tử trên màn hình điện thoại",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = SwbYellow)
                    ) {
                        Text("Cấp quyền máy ảnh", color = SwbNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. HUD Scanner Overlay (Viewfinder, Flash Border, HUD Controls, Result Card)
        ScannerOverlay(
            isTorchOn = isTorchOn,
            isContinuousMode = isContinuousMode,
            zoomRatio = zoomRatio,
            isFlashingBorder = isFlashingBorder,
            flashColor = flashColor,
            lastScanEvent = lastScanEvent,
            showResultCard = showResultCard,
            onToggleTorch = { viewModel.toggleTorch() },
            onToggleContinuous = { viewModel.toggleContinuousMode() },
            onSetZoom = { ratio -> viewModel.setZoom(ratio) },
            onManualInputClick = { showManualDialog = true },
            onDismissCard = { viewModel.dismissResultCard() },
            onOpenVaultClick = onOpenVault
        )

        // 3. Top Translucent Status & Station Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            SwbNavy.copy(alpha = 0.95f),
                            SwbNavy.copy(alpha = 0.75f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Station Selector Pill
                Box {
                    Surface(
                        onClick = { showStationMenu = true },
                        shape = RoundedCornerShape(16.dp),
                        color = SwbDeepBlue,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwbCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SwbCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentStation,
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStationMenu,
                        onDismissRequest = { showStationMenu = false }
                    ) {
                        SwbStations.ALL_STATIONS.forEach { station ->
                            DropdownMenuItem(
                                text = { Text(station) },
                                onClick = {
                                    viewModel.setStation(station)
                                    showStationMenu = false
                                }
                            )
                        }
                    }
                }

                // Trip Info Selector Pill
                Box {
                    Surface(
                        onClick = { showTripMenu = true },
                        shape = RoundedCornerShape(16.dp),
                        color = SwbDeepBlue,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwbYellow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = SwbYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentTrip.tripCode} (${currentTrip.departureTime})",
                                color = SwbYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showTripMenu,
                        onDismissRequest = { showTripMenu = false }
                    ) {
                        SwbTrips.TRIPS.forEach { trip ->
                            DropdownMenuItem(
                                text = { Text("${trip.tripCode}: ${trip.route} (${trip.departureTime})") },
                                onClick = {
                                    viewModel.setTrip(trip)
                                    showTripMenu = false
                                }
                            )
                        }
                    }
                }

                // Boarding Count Badge
                Surface(
                    color = SwbSurfaceBlue,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SwbValidGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$checkedCount/$totalCount",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Test Tickets Carousel for seamless testing right from the scanner screen!
            Spacer(modifier = Modifier.height(6.dp))
            QuickTestTicketsBar(
                tickets = tickets.filter { it.status == TicketStatus.PENDING.name }.take(8),
                onSimulateScan = { code -> viewModel.simulateScan(code) },
                onOpenVault = onOpenVault
            )
        }
    }

    // Manual Ticket Code Input Dialog
    if (showManualDialog) {
        ManualCodeInputDialog(
            onDismiss = { showManualDialog = false },
            onSubmitCode = { code ->
                viewModel.onQrScanned(code)
                showManualDialog = false
            }
        )
    }
}

@Composable
fun QuickTestTicketsBar(
    tickets: List<com.example.data.model.TicketEntity>,
    onSimulateScan: (String) -> Unit,
    onOpenVault: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Surface(
                onClick = onOpenVault,
                color = SwbYellow,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = SwbNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kho Mã QR (+Tạo)",
                        color = SwbNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(tickets) { ticket ->
            Surface(
                onClick = { onSimulateScan(ticket.code) },
                color = SwbDeepBlue.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SwbCyan.copy(alpha = 0.4f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Quét: ${ticket.seatNumber} (${ticket.passengerCategory})",
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ManualCodeInputDialog(
    onDismiss: () -> Unit,
    onSubmitCode: (String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nhập Mã Vé Thủ Công",
                color = SwbYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Dùng khi vé giấy in nhiệt bị mờ, rách hoặc màn hình khách quá trầy xước",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    placeholder = { Text("VD: SWB-BDLD-20261112-A01") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = SwbCyan,
                        unfocusedBorderColor = SwbSurfaceBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (codeInput.isNotBlank()) {
                        onSubmitCode(codeInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwbValidGreen)
            ) {
                Text("Xác thực vé", color = SwbNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy", color = TextSecondary)
            }
        },
        containerColor = SwbDeepBlue
    )
}
