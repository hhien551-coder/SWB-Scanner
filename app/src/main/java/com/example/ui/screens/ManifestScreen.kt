package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.ui.theme.SwbCardBlue
import com.example.ui.theme.SwbCyan
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestScreen(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit
) {
    val tickets by viewModel.allTickets.collectAsState()
    val currentTrip by viewModel.currentTrip.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, CHECKED_IN, PENDING
    var showResetDialog by remember { mutableStateOf(false) }

    val checkedInCount = tickets.count { it.status == TicketStatus.CHECKED_IN.name }
    val totalCount = tickets.size.coerceAtLeast(1)
    val progress = checkedInCount.toFloat() / totalCount.toFloat()

    val filteredList = tickets.filter { ticket ->
        val matchesSearch = searchQuery.isBlank() ||
                ticket.passengerCategory.contains(searchQuery, ignoreCase = true) ||
                ticket.seatNumber.contains(searchQuery, ignoreCase = true) ||
                ticket.code.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filterStatus) {
            "CHECKED_IN" -> ticket.status == TicketStatus.CHECKED_IN.name
            "PENDING" -> ticket.status == TicketStatus.PENDING.name
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Danh Sách Lên Tàu (Manifest)",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentTrip.boatName} • ${currentTrip.departureTime}",
                            color = SwbCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_manifest_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới lượt soát",
                            tint = SwbYellow
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
            // Live Boarding Progress Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SwbDeepBlue),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, SwbCyan.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = SwbYellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tiến độ đón khách",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = "$checkedInCount / $totalCount khách (${(progress * 100).toInt()}%)",
                            color = SwbYellowLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SwbValidGreen,
                        trackColor = SwbSurfaceBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BoardingStatBadge("Đã lên tàu", "$checkedInCount", SwbValidGreen)
                        BoardingStatBadge("Chưa soát", "${totalCount - checkedInCount}", SwbYellow)
                        BoardingStatBadge("Bến hiện tại", currentStation.replace("Bến ", ""), SwbCyan)
                    }
                }
            }

            // Search Bar & Filter Chips
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo tên, ghế A-01, mã vé...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SwbCyan
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = SwbCyan,
                        unfocusedBorderColor = SwbSurfaceBlue,
                        focusedContainerColor = SwbDeepBlue,
                        unfocusedContainerColor = SwbDeepBlue
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "Tất cả ($totalCount)", "CHECKED_IN" to "Đã lên ($checkedInCount)", "PENDING" to "Chưa lên (${totalCount - checkedInCount})").forEach { (key, label) ->
                        val isSelected = filterStatus == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterStatus = key },
                            label = { Text(label, fontSize = 12.sp) },
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Passenger List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.code }) { ticket ->
                    ManifestPassengerCard(
                        ticket = ticket,
                        onRevert = { viewModel.revertCheckIn(ticket.code) },
                        onManualCheckIn = { viewModel.simulateScan(ticket.code) }
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Làm mới lượt soát vé?", color = SwbYellow, fontWeight = FontWeight.Bold) },
            text = { Text("Tất cả vé sẽ được đưa về trạng thái Chưa soát để bắt đầu chuyến mới.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllBoarding()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwbYellow)
                ) {
                    Text("Đồng ý", color = SwbNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            containerColor = SwbDeepBlue
        )
    }
}

@Composable
fun ManifestPassengerCard(
    ticket: TicketEntity,
    onRevert: () -> Unit,
    onManualCheckIn: () -> Unit
) {
    val isCheckedIn = ticket.status == TicketStatus.CHECKED_IN.name

    Card(
        colors = CardDefaults.cardColors(containerColor = SwbCardBlue),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCheckedIn) SwbValidGreen.copy(alpha = 0.4f) else SwbSurfaceBlue,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seat Number Circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isCheckedIn) SwbValidGreen else SwbSurfaceBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ticket.seatNumber,
                    color = if (isCheckedIn) SwbNavy else SwbYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.passengerCategory,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${ticket.fromStation} ➔ ${ticket.toStation}",
                    color = SwbCyan,
                    fontSize = 12.sp
                )
                if (isCheckedIn && ticket.checkedInAt != null) {
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ticket.checkedInAt))
                    Text(
                        text = "Đã quét lúc: $timeStr tại ${ticket.checkedInStation ?: "Cổng"}",
                        color = SwbValidGreen,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "Mã: ${ticket.code}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Action: Check-in / Undo
            if (isCheckedIn) {
                IconButton(
                    onClick = onRevert,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Hủy soát",
                        tint = TextSecondary
                    )
                }
            } else {
                Button(
                    onClick = onManualCheckIn,
                    colors = ButtonDefaults.buttonColors(containerColor = SwbValidGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Soát vé", color = SwbNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BoardingStatBadge(label: String, value: String, color: Color) {
    Surface(
        color = SwbSurfaceBlue,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
