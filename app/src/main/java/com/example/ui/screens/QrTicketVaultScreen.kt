package com.example.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PassengerCategory
import com.example.data.model.SwbStations
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.ui.theme.SwbCardBlue
import com.example.ui.theme.SwbCyan
import com.example.ui.theme.SwbCyanLight
import com.example.ui.theme.SwbDeepBlue
import com.example.ui.theme.SwbNavy
import com.example.ui.theme.SwbSurfaceBlue
import com.example.ui.theme.SwbValidGreen
import com.example.ui.theme.SwbYellow
import com.example.ui.theme.SwbYellowLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ScannerViewModel
import com.example.util.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrTicketVaultScreen(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit
) {
    val tickets by viewModel.allTickets.collectAsState()
    var selectedStationFilter by remember { mutableStateOf("Tất cả") }
    var selectedStatusFilter by remember { mutableStateOf("PENDING") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // ALL, ADULT, CHILD
    var showCreateDialog by remember { mutableStateOf(false) }
    var fullscreenTicket by remember { mutableStateOf<TicketEntity?>(null) }

    val stationOptions = listOf("Tất cả") + SwbStations.ALL_STATIONS

    val filteredTickets = tickets.filter { ticket ->
        val stationMatch = if (selectedStationFilter == "Tất cả") true else ticket.fromStation == selectedStationFilter
        val statusMatch = when (selectedStatusFilter) {
            "PENDING" -> ticket.status == TicketStatus.PENDING.name
            "CHECKED_IN" -> ticket.status == TicketStatus.CHECKED_IN.name
            else -> true
        }
        val categoryMatch = when (selectedCategoryFilter) {
            "ADULT" -> ticket.passengerCategory == PassengerCategory.ADULT
            "CHILD" -> ticket.passengerCategory == PassengerCategory.CHILD
            else -> true
        }
        stationMatch && statusMatch && categoryMatch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kho Mã Vé QR Hợp Lệ",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Phân loại Vé Người Lớn & Vé Trẻ Em",
                            color = SwbCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("vault_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng kho vé",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SwbYellow),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("create_ticket_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = SwbNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tạo vé mới",
                            color = SwbNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwbDeepBlue
                )
            )
        },
        containerColor = SwbNavy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Quick Filter (Tất cả / Vé người lớn / Vé trẻ em)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "Tất cả (${tickets.size})",
                    "ADULT" to "Vé người lớn (${tickets.count { it.passengerCategory == PassengerCategory.ADULT }})",
                    "CHILD" to "Vé trẻ em (${tickets.count { it.passengerCategory == PassengerCategory.CHILD }})"
                ).forEach { (catKey, label) ->
                    val isSelected = selectedCategoryFilter == catKey
                    Surface(
                        onClick = { selectedCategoryFilter = catKey },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SwbYellow else SwbSurfaceBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) SwbNavy else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Station Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stationOptions) { station ->
                    val isSelected = selectedStationFilter == station
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStationFilter = station },
                        label = {
                            Text(
                                text = station.replace("Bến ", ""),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SwbCyan,
                            selectedLabelColor = SwbNavy,
                            containerColor = SwbSurfaceBlue,
                            labelColor = TextWhite
                        ),
                        border = null
                    )
                }
            }

            // Ticket List with Live QR Codes
            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Không có vé nào trong danh mục này",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SwbYellow)
                        ) {
                            Text("Tạo mã vé mới", color = SwbNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTickets, key = { it.code }) { ticket ->
                        QrTicketItemCard(
                            ticket = ticket,
                            onSimulateScan = {
                                viewModel.simulateScan(ticket.code)
                            },
                            onFullscreenClick = {
                                fullscreenTicket = ticket
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog to create a brand new valid QR ticket (Người lớn / Trẻ em)
    if (showCreateDialog) {
        CreateTicketDialog(
            currentStation = viewModel.currentStation.collectAsState().value,
            onDismiss = { showCreateDialog = false },
            onCreateTicket = { category, seat, from, to, type ->
                viewModel.createAndAddTicket(category, seat, from, to, type)
                showCreateDialog = false
            }
        )
    }

    // Fullscreen QR Code viewer for effortless scanning from other screens or phones
    fullscreenTicket?.let { ticket ->
        FullscreenQrDialog(
            ticket = ticket,
            onDismiss = { fullscreenTicket = null },
            onSimulateScan = {
                viewModel.simulateScan(ticket.code)
                fullscreenTicket = null
            }
        )
    }
}

@Composable
fun QrTicketItemCard(
    ticket: TicketEntity,
    onSimulateScan: () -> Unit,
    onFullscreenClick: () -> Unit
) {
    val qrBitmap = remember(ticket.code) {
        QrCodeGenerator.generateQrBitmap(ticket.code, width = 320, height = 320)
    }

    val isCheckedIn = ticket.status == TicketStatus.CHECKED_IN.name
    val isChild = ticket.passengerCategory == PassengerCategory.CHILD

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SwbCardBlue
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCheckedIn) SwbValidGreen.copy(alpha = 0.5f) else SwbCyan.copy(alpha = 0.3f),
                RoundedCornerShape(18.dp)
            )
            .testTag("ticket_card_${ticket.seatNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Code Bitmap Thumbnail
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onFullscreenClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Mã QR vé ${ticket.code}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Ticket details (No personal name - strictly Vé người lớn or Vé trẻ em)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ticket.passengerCategory,
                        color = if (isChild) SwbYellow else SwbCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Surface(
                        color = if (isCheckedIn) SwbValidGreen else SwbYellow,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isCheckedIn) "ĐÃ SOÁT" else "GHẾ ${ticket.seatNumber}",
                            color = SwbNavy,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${ticket.fromStation} ➔ ${ticket.toStation}",
                    color = SwbCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${ticket.boatName} • ${ticket.departureTime}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = ticket.code,
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Simulate scan & Fullscreen view
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSimulateScan,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCheckedIn) SwbSurfaceBlue else SwbValidGreen
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isCheckedIn) TextWhite else SwbNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCheckedIn) "Quét lại" else "Quét thử ngay",
                            color = if (isCheckedIn) TextWhite else SwbNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onFullscreenClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwbCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = SwbCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Phóng to QR",
                            color = SwbCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenQrDialog(
    ticket: TicketEntity,
    onDismiss: () -> Unit,
    onSimulateScan: () -> Unit
) {
    val qrBitmap = remember(ticket.code) {
        QrCodeGenerator.generateQrBitmap(ticket.code, width = 600, height = 600)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SwbDeepBlue),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(2.dp, SwbYellow, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VÉ ĐIỆN TỬ SAIGON WATERBUS",
                        color = SwbYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-contrast QR Container for phone/thermal paper scanning
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(230.dp)
                        .padding(4.dp)
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code lớn",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = ticket.passengerCategory,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "SỐ GHẾ: ",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = ticket.seatNumber,
                        color = SwbYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "${ticket.fromStation} ➔ ${ticket.toStation}",
                    color = SwbCyan,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "Giờ xuất bến: ${ticket.departureTime} • ${ticket.boatName}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = ticket.code,
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onSimulateScan,
                    colors = ButtonDefaults.buttonColors(containerColor = SwbValidGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = SwbNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mô phỏng quét vé này",
                        color = SwbNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CreateTicketDialog(
    currentStation: String,
    onDismiss: () -> Unit,
    onCreateTicket: (category: String, seat: String, from: String, to: String, type: TicketType) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(PassengerCategory.ADULT) }
    var seat by remember { mutableStateOf("A-${(10..99).random()}") }
    var fromStation by remember { mutableStateOf(currentStation) }
    var toStation by remember { mutableStateOf(SwbStations.LINH_DONG) }
    var selectedType by remember { mutableStateOf(TicketType.STANDARD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tạo Vé Điện Tử QR Hợp Lệ Mới",
                color = SwbYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category Picker: Vé người lớn vs Vé trẻ em
                Text(
                    text = "Đối tượng vé:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PassengerCategory.ADULT, PassengerCategory.CHILD).forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SwbYellow else SwbSurfaceBlue,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) SwbNavy else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = seat,
                    onValueChange = { seat = it },
                    label = { Text("Số ghế") },
                    placeholder = { Text("VD: A-15") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = SwbCyan,
                        unfocusedBorderColor = SwbSurfaceBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Station selection
                Text(
                    text = "Bến khởi hành: $fromStation",
                    color = SwbCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Ticket Type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TicketType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SwbYellow else SwbSurfaceBlue)
                                .clickable { selectedType = type }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (type) {
                                    TicketType.STANDARD -> "Thường"
                                    TicketType.VIP_DECK -> "VIP"
                                    TicketType.ROUND_TRIP -> "Khứ hồi"
                                    TicketType.DISCOUNT -> "Giảm giá"
                                },
                                color = if (isSelected) SwbNavy else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateTicket(selectedCategory, seat, fromStation, toStation, selectedType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwbYellow)
            ) {
                Text("Tạo vé QR ngay", color = SwbNavy, fontWeight = FontWeight.Bold)
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
