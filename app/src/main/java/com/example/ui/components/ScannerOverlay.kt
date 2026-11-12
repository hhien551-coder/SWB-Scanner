package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.SwbYellowLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite

@Composable
fun ScannerOverlay(
    isTorchOn: Boolean,
    isContinuousMode: Boolean,
    zoomRatio: Float,
    isFlashingBorder: Boolean,
    flashColor: Color,
    lastScanEvent: ScanEvent?,
    showResultCard: Boolean,
    onToggleTorch: () -> Unit,
    onToggleContinuous: () -> Unit,
    onSetZoom: (Float) -> Unit,
    onManualInputClick: () -> Unit,
    onDismissCard: () -> Unit,
    onOpenVaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("scanner_overlay_container")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val boxSize = (screenWidth * 0.72f).coerceIn(240.dp, 310.dp)

        // 1. Fullscreen Multi-sensory Visual Flash Border
        if (isFlashingBorder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(10.dp, flashColor, RoundedCornerShape(0.dp))
                    .background(flashColor.copy(alpha = 0.22f))
            )
        }

        // 2. Camera Viewfinder Cutout and Guides
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(boxSize)
        ) {
            // Viewfinder Corner Brackets
            CornerBrackets(
                color = if (isFlashingBorder) flashColor else SwbCyan,
                modifier = Modifier.fillMaxSize()
            )

            // Animated Laser Scanning Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = boxSize * laserOffset)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                if (isContinuousMode) SwbCyan else SwbYellow,
                                Color.White,
                                if (isContinuousMode) SwbCyan else SwbYellow,
                                Color.Transparent
                            )
                        )
                    )
            )

            // Center crosshair / alignment hint
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .border(1.dp, SwbCyan.copy(alpha = 0.4f), CircleShape)
            )
        }

        // 3. Viewfinder Sunlight & Guidance Tip
        Surface(
            color = SwbNavy.copy(alpha = 0.85f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -(boxSize / 2 + 36.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isContinuousMode) SwbValidGreen else SwbYellow,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isContinuousMode) "QUÉT LIÊN TỤC • TỰ LẤY NÉT & BÙ SÁNG" else "QUÉT TỪNG VÉ",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Floating Action Controls Bar (Torch, Continuous Mode, Zoom, Manual Input)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (showResultCard) 260.dp else 24.dp)
                .background(SwbNavy.copy(alpha = 0.88f), RoundedCornerShape(28.dp))
                .border(1.dp, SwbCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flashlight / Sunlight Glare Compensation Toggle
            IconButton(
                onClick = onToggleTorch,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isTorchOn) SwbYellow else Color.Transparent,
                        CircleShape
                    )
                    .testTag("torch_toggle_button")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Bật/Tắt đèn bù sáng",
                    tint = if (isTorchOn) SwbNavy else TextWhite
                )
            }

            // Continuous Mode Toggle
            Surface(
                onClick = onToggleContinuous,
                shape = RoundedCornerShape(20.dp),
                color = if (isContinuousMode) SwbCyan else SwbSurfaceBlue,
                modifier = Modifier.testTag("continuous_mode_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isContinuousMode) "⚡ Quét nhanh" else "Chế độ thường",
                        color = if (isContinuousMode) SwbNavy else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Zoom Selector (1x, 1.5x, 2x)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SwbSurfaceBlue)
                    .padding(2.dp)
            ) {
                listOf(0.0f to "1x", 0.25f to "1.5x", 0.5f to "2x").forEach { (ratio, label) ->
                    val isSelected = zoomRatio == ratio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) SwbYellow else Color.Transparent)
                            .clickable { onSetZoom(ratio) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) SwbNavy else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Manual Code Entry
            IconButton(
                onClick = onManualInputClick,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("manual_input_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Nhập mã vé thủ công",
                    tint = SwbCyan
                )
            }

            // Open QR Vault
            IconButton(
                onClick = onOpenVaultClick,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("qr_vault_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Kho mã QR vé",
                    tint = SwbYellow
                )
            }
        }

        // 5. Instant Multi-sensory Feedback Result Card
        AnimatedVisibility(
            visible = showResultCard && lastScanEvent != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            lastScanEvent?.let { event ->
                InstantResultCard(
                    event = event,
                    onDismiss = onDismissCard
                )
            }
        }
    }
}

@Composable
fun InstantResultCard(
    event: ScanEvent,
    onDismiss: () -> Unit
) {
    val isSuccess = event.status == ValidationStatus.VALID
    val isAlreadyUsed = event.status == ValidationStatus.ALREADY_CHECKED_IN
    val ticket = event.ticket

    val badgeColor = when {
        isSuccess -> SwbValidGreen
        isAlreadyUsed -> SwbErrorRed
        else -> SwbYellow
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SwbCardBlue
        ),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, badgeColor, RoundedCornerShape(22.dp))
            .testTag("instant_result_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Status & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            isSuccess -> Icons.Default.CheckCircle
                            isAlreadyUsed -> Icons.Default.Warning
                            else -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (event.status) {
                            ValidationStatus.VALID -> "VÉ HỢP LỆ • MỜI LÊN TÀU"
                            ValidationStatus.ALREADY_CHECKED_IN -> "CẢNH BÁO: VÉ ĐÃ SỬ DỤNG"
                            ValidationStatus.WRONG_TRIP -> "SAI CHUYẾN TÀU"
                            ValidationStatus.WRONG_STATION -> "SAI BẾN KHỞI HÀNH"
                            ValidationStatus.NOT_FOUND -> "KHÔNG TÌM THẤY VÉ"
                            ValidationStatus.INVALID_FORMAT -> "MÃ QR KHÔNG HỢP LỆ"
                        },
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Passenger & Seat info
            if (ticket != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ticket.passengerCategory,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ticket.fromStation} ➔ ${ticket.toStation}",
                            color = SwbCyan,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${ticket.boatName} • Chuyến ${ticket.departureTime}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Big Seat Number Badge
                    Surface(
                        color = SwbDeepBlue,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwbYellow.copy(alpha = 0.7f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "SỐ GHẾ",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = ticket.seatNumber,
                                color = SwbYellow,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SwbNavy.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mã vé: ${ticket.code}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Loại vé: ${ticket.ticketType}",
                        color = SwbYellowLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = event.message,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Mã quét: ${event.rawCode}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-second countdown indicator
            val countdownAnim = remember(event.timestamp) { androidx.compose.animation.core.Animatable(1f) }
            LaunchedEffect(event.timestamp) {
                countdownAnim.snapTo(1f)
                countdownAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(3000, easing = androidx.compose.animation.core.LinearEasing)
                )
            }

            androidx.compose.material3.LinearProgressIndicator(
                progress = { countdownAnim.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = badgeColor,
                trackColor = SwbSurfaceBlue
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "✓ Soát vé thành công",
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Quét lại sau 3 giây",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CornerBrackets(
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 10f,
    bracketLength: Float = 55f
) {
    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height

            // Top-Left
            drawLine(color, Offset(0f, 0f), Offset(bracketLength, 0f), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(0f, bracketLength), strokeWidth)

            // Top-Right
            drawLine(color, Offset(w, 0f), Offset(w - bracketLength, 0f), strokeWidth)
            drawLine(color, Offset(w, 0f), Offset(w, bracketLength), strokeWidth)

            // Bottom-Left
            drawLine(color, Offset(0f, h), Offset(bracketLength, h), strokeWidth)
            drawLine(color, Offset(0f, h), Offset(0f, h - bracketLength), strokeWidth)

            // Bottom-Right
            drawLine(color, Offset(w, h), Offset(w - bracketLength, h), strokeWidth)
            drawLine(color, Offset(w, h), Offset(w, h - bracketLength), strokeWidth)
        }
    )
}
