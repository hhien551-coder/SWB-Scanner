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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanEvent
import com.example.data.model.ValidationStatus
import com.example.ui.theme.SwbCardBlue
import com.example.ui.theme.SwbCyan
import com.example.ui.theme.SwbDeepBlue
import com.example.ui.theme.SwbErrorRed
import com.example.ui.theme.SwbNavy
import com.example.ui.theme.SwbSurfaceBlue
import com.example.ui.theme.SwbValidGreen
import com.example.ui.theme.SwbYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ScannerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ScannerViewModel
) {
    val history by viewModel.scanHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nhật Ký Quét Vé",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lịch sử kiểm soát vé thời gian thực",
                            color = SwbCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwbDeepBlue)
            )
        },
        containerColor = SwbNavy
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có lượt quét nào trong phiên làm việc này",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(history) { event ->
                    HistoryItemCard(event)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(event: ScanEvent) {
    val isSuccess = event.status == ValidationStatus.VALID
    val isAlreadyUsed = event.status == ValidationStatus.ALREADY_CHECKED_IN
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))

    val color = when {
        isSuccess -> SwbValidGreen
        isAlreadyUsed -> SwbErrorRed
        else -> SwbYellow
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SwbCardBlue),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isSuccess -> Icons.Default.CheckCircle
                    isAlreadyUsed -> Icons.Default.Warning
                    else -> Icons.Default.Error
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.ticket?.passengerCategory ?: "Mã: ${event.rawCode}",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = timeStr,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = event.message,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                if (event.ticket != null) {
                    Text(
                        text = "Ghế ${event.ticket.seatNumber} • ${event.ticket.fromStation} ➔ ${event.ticket.toStation}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
