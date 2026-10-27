package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.WatchlistEntity
import com.example.data.repository.ScannedAssetData
import com.example.ui.MainTab
import com.example.ui.ScoutUiState
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SignalStatusBadge
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ScannerSortBy {
    SCORE,
    VOLUME,
    CHANGE
}

@Composable
fun ScannerScreen(
    uiState: ScoutUiState,
    watchlist: List<WatchlistEntity>,
    onSelectAsset: (String) -> Unit,
    onToggleWatchlist: (String, String) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleFilterPassed: () -> Unit
) {
    var sortBy by remember { mutableStateOf(ScannerSortBy.SCORE) }

    val filteredList = uiState.scannedAssets
        .filter { asset ->
            val matchSearch = asset.ticker.symbol.contains(uiState.searchQuery, ignoreCase = true) ||
                              asset.ticker.baseAsset.contains(uiState.searchQuery, ignoreCase = true)
            val matchFilter = if (uiState.filterOnlyPassed) asset.filterResult.isPassed else true
            matchSearch && matchFilter
        }
        .sortedWith { a, b ->
            when (sortBy) {
                ScannerSortBy.SCORE -> b.scoreBreakdown.totalScore.compareTo(a.scoreBreakdown.totalScore)
                ScannerSortBy.VOLUME -> b.ticker.volume24hUsd.compareTo(a.ticker.volume24hUsd)
                ScannerSortBy.CHANGE -> b.ticker.priceChangePercent.compareTo(a.ticker.priceChangePercent)
            }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pemindai Likuiditas & Keamanan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Menyaring seluruh pasangan spot berdasarkan usia candle, volume, dan reliabilitas.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanner_search_input"),
                placeholder = { Text("Cari koin (contoh: BTC, SOL, NEAR)...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = RadarCyan,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Filter and Sort Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.filterOnlyPassed,
                    onClick = onToggleFilterPassed,
                    label = { Text("Lolos Filter Saja", fontSize = 11.sp) },
                    leadingIcon = {
                        if (uiState.filterOnlyPassed) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BullGreen.copy(alpha = 0.2f),
                        selectedLabelColor = BullGreen,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("chip_filter_passed")
                )

                FilterChip(
                    selected = sortBy == ScannerSortBy.SCORE,
                    onClick = { sortBy = ScannerSortBy.SCORE },
                    label = { Text("Skor", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadarCyan.copy(alpha = 0.2f),
                        selectedLabelColor = RadarCyan,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = sortBy == ScannerSortBy.VOLUME,
                    onClick = { sortBy = ScannerSortBy.VOLUME },
                    label = { Text("Volume", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadarCyan.copy(alpha = 0.2f),
                        selectedLabelColor = RadarCyan,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = sortBy == ScannerSortBy.CHANGE,
                    onClick = { sortBy = ScannerSortBy.CHANGE },
                    label = { Text("24h %", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadarCyan.copy(alpha = 0.2f),
                        selectedLabelColor = RadarCyan,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        item {
            Text(
                text = "Ditemukan ${filteredList.size} Aset Spot",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
        }

        // List of Scanned Assets
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    color = DarkSurface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tidak ada aset yang sesuai kriteria", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(filteredList) { asset ->
                val isSaved = watchlist.any { it.symbol == asset.ticker.symbol }
                ScannedAssetCard(
                    asset = asset,
                    isWatchlisted = isSaved,
                    onToggleWatchlist = { onToggleWatchlist(asset.ticker.symbol, asset.ticker.baseAsset) },
                    onSelect = {
                        onSelectAsset(asset.ticker.symbol)
                        onNavigateTab(MainTab.CALCULATOR)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ScannedAssetCard(
    asset: ScannedAssetData,
    isWatchlisted: Boolean,
    onToggleWatchlist: () -> Unit,
    onSelect: () -> Unit
) {
    val ticker = asset.ticker
    val filter = asset.filterResult
    val signal = asset.signal

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (filter.isPassed) DarkSurfaceBorder else BearRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("scanned_card_${ticker.symbol}"),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Symbol, Watchlist Button, Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ticker.baseAsset,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/USDT",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "Vol 24h: $${String.format("%,.0f", ticker.volume24hUsd / 1_000_000.0)}M • Hist: ${ticker.daysHistory}h",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.size(32.dp).testTag("btn_watchlist_${ticker.symbol}")
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (isWatchlisted) AlertYellow else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    ScoreBadge(score = asset.scoreBreakdown.totalScore)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Harga & Status Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$${if (ticker.lastPrice >= 1.0) String.format("%.2f", ticker.lastPrice) else String.format("%.4f", ticker.lastPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${if (ticker.priceChangePercent >= 0) "+" else ""}${String.format("%.2f", ticker.priceChangePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ticker.priceChangePercent >= 0) BullGreen else BearRed
                    )
                }

                if (signal != null) {
                    SignalStatusBadge(status = signal.status)
                } else if (!filter.isPassed) {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                        color = BearRed.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BearRed.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Gagal Filter Likuiditas",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BearRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Faktor Kegagalan jika tidak lolos
            if (!filter.isPassed && filter.failureReasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BearRed.copy(alpha = 0.08f))
                        .padding(6.dp)
                ) {
                    Column {
                        Text("Faktor Kegagalan:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BearRed)
                        filter.failureReasons.forEach { reason ->
                            Text("• $reason", fontSize = 9.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Status Keamanan Token
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = BullGreen, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Keamanan: ${ticker.securityStatus}", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}
