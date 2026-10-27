package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CryptoScoutViewModel
import com.example.ui.MainTab
import com.example.ui.screens.BacktestSettingsScreen
import com.example.ui.screens.CalculatorChartScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SignalDetailScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CryptoScoutTheme
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: CryptoScoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoScoutTheme {
                val uiState by viewModel.uiState.collectAsState()
                val watchlist by viewModel.watchlist.collectAsState()
                val signalHistory by viewModel.signalHistory.collectAsState()

                // Tangani tombol Back fisik/gesture: Jika bukan di Dashboard, kembalikan ke Dashboard
                BackHandler(enabled = uiState.activeTab != MainTab.DASHBOARD) {
                    viewModel.setTab(MainTab.DASHBOARD)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = DarkBg,
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_navigation_bar"),
                            containerColor = DarkSurface,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                Triple(MainTab.DASHBOARD, Icons.Default.Dashboard, "Beranda"),
                                Triple(MainTab.SCANNER, Icons.Default.FilterList, "Pemindai"),
                                Triple(MainTab.SIGNALS, Icons.AutoMirrored.Filled.TrendingUp, "Sinyal"),
                                Triple(MainTab.CALCULATOR, Icons.Default.Calculate, "Risiko"),
                                Triple(MainTab.WATCHLIST, Icons.Default.Bookmark, "Watchlist"),
                                Triple(MainTab.SETTINGS, Icons.Default.Tune, "Opsi")
                            )

                            items.forEach { (tab, icon, label) ->
                                val selected = uiState.activeTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.setTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DarkBg,
                                        selectedTextColor = BullGreen,
                                        indicatorColor = BullGreen,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DarkBg)
                    ) {
                        if (uiState.isLoading && uiState.scannedAssets.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.layout.Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = BullGreen,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                                    Text(
                                        text = "Crypto Scout AI Memindai Pasar...",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else {
                            when (uiState.activeTab) {
                                MainTab.DASHBOARD -> DashboardScreen(
                                    uiState = uiState,
                                    onRefresh = { viewModel.refreshScan() },
                                    onNavigateTab = { viewModel.setTab(it) },
                                    onSelectAsset = { viewModel.selectAsset(it) }
                                )

                                MainTab.SCANNER -> ScannerScreen(
                                    uiState = uiState,
                                    watchlist = watchlist,
                                    onSelectAsset = { viewModel.selectAsset(it) },
                                    onToggleWatchlist = { sym, base -> viewModel.toggleWatchlist(sym, base) },
                                    onNavigateTab = { viewModel.setTab(it) },
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onToggleFilterPassed = { viewModel.toggleFilterOnlyPassed() }
                                )

                                MainTab.SIGNALS -> SignalDetailScreen(
                                    uiState = uiState,
                                    onSelectAsset = { viewModel.selectAsset(it) },
                                    onNavigateTab = { viewModel.setTab(it) }
                                )

                                MainTab.CALCULATOR -> CalculatorChartScreen(
                                    uiState = uiState,
                                    watchlist = watchlist,
                                    onToggleWatchlist = { sym, base -> viewModel.toggleWatchlist(sym, base) },
                                    onUpdateRiskParameters = { cap, risk, rate ->
                                        viewModel.updateRiskParameters(cap, risk, rate)
                                    },
                                    onUpdateCustomPosition = { entry, sl ->
                                        viewModel.updateCustomPosition(entry, sl)
                                    }
                                )

                                MainTab.WATCHLIST -> WatchlistScreen(
                                    uiState = uiState,
                                    watchlist = watchlist,
                                    signalHistory = signalHistory,
                                    onToggleWatchlist = { sym, base -> viewModel.toggleWatchlist(sym, base) },
                                    onSelectAsset = { viewModel.selectAsset(it) },
                                    onNavigateTab = { viewModel.setTab(it) },
                                    onExportCsv = { viewModel.exportSignalHistoryCsv() },
                                    onClearCsv = { viewModel.clearCsvExport() }
                                )

                                MainTab.SETTINGS -> BacktestSettingsScreen(
                                    uiState = uiState,
                                    onRunBacktest = { viewModel.runBacktestForSelected() },
                                    onUpdateFilterSettings = { viewModel.updateFilterSettings(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
