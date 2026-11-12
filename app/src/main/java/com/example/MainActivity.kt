package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ManifestScreen
import com.example.ui.screens.QrTicketVaultScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SwbCyan
import com.example.ui.theme.SwbDeepBlue
import com.example.ui.theme.SwbNavy
import com.example.ui.theme.SwbYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ScannerViewModel

enum class MainTab {
    SCANNER,
    QR_VAULT,
    MANIFEST,
    HISTORY
}

class MainActivity : ComponentActivity() {

    private val viewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ScannerViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.SCANNER) }

    BackHandler(enabled = currentTab != MainTab.SCANNER) {
        currentTab = MainTab.SCANNER
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SwbNavy,
        bottomBar = {
            NavigationBar(
                containerColor = SwbDeepBlue,
                contentColor = TextWhite,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.SCANNER,
                    onClick = { currentTab = MainTab.SCANNER },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Soát vé QR"
                        )
                    },
                    label = {
                        Text(
                            text = "Soát vé",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.SCANNER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SwbNavy,
                        selectedTextColor = SwbYellow,
                        indicatorColor = SwbYellow,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_scanner")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.QR_VAULT,
                    onClick = { currentTab = MainTab.QR_VAULT },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Kho mã QR vé"
                        )
                    },
                    label = {
                        Text(
                            text = "Mã QR vé",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.QR_VAULT) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SwbNavy,
                        selectedTextColor = SwbCyan,
                        indicatorColor = SwbCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_qr_vault")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.MANIFEST,
                    onClick = { currentTab = MainTab.MANIFEST },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "Danh sách lên tàu"
                        )
                    },
                    label = {
                        Text(
                            text = "Lên tàu",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.MANIFEST) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SwbNavy,
                        selectedTextColor = SwbYellow,
                        indicatorColor = SwbYellow,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_manifest")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.HISTORY,
                    onClick = { currentTab = MainTab.HISTORY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Lịch sử soát"
                        )
                    },
                    label = {
                        Text(
                            text = "Nhật ký",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SwbNavy,
                        selectedTextColor = SwbCyan,
                        indicatorColor = SwbCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.SCANNER -> {
                        ScannerScreen(
                            viewModel = viewModel,
                            onOpenVault = { currentTab = MainTab.QR_VAULT },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MainTab.QR_VAULT -> {
                        QrTicketVaultScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentTab = MainTab.SCANNER }
                        )
                    }
                    MainTab.MANIFEST -> {
                        ManifestScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentTab = MainTab.SCANNER }
                        )
                    }
                    MainTab.HISTORY -> {
                        HistoryScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
