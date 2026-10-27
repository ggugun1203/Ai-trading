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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalStatus
import com.example.data.repository.ScannedAssetData
import com.example.ui.MainTab
import com.example.ui.ScoutUiState
import com.example.ui.components.ApiStatusIndicator
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SignalStatusBadge
import com.example.ui.components.SystemDisclaimerBanner
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: ScoutUiState,
    onRefresh: () -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    onSelectAsset: (String) -> Unit
) {
    val overview = uiState.marketOverview

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Crypto Scout AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pemindai Swing Trading Spot Kripto",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ApiStatusIndicator(statusText = overview?.apiStatus ?: "Menghubungkan...")
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .testTag("refresh_button")
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = RadarCyan, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Perbarui", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        // Disclaimer
        item {
            SystemDisclaimerBanner()
        }

        // Market Overview Cards: BTC & ETH
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BTC Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    color = DarkSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BTC / USDT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            val change = overview?.btcChange24h ?: 0.0
                            Text(
                                text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (change >= 0) BullGreen else BearRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%,.0f", overview?.btcPrice ?: 0.0)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }

                // ETH Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    color = DarkSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ETH / USDT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            val change = overview?.ethChange24h ?: 0.0
                            Text(
                                text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (change >= 0) BullGreen else BearRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%,.2f", overview?.ethPrice ?: 0.0)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Market Regime & Metrics Grid
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Regim Pasar Kripto", fontSize = 12.sp, color = TextSecondary)
                        val timeStr = if (overview != null) {
                            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(overview.lastUpdateTime))
                        } else "--:--"
                        Text("Update: $timeStr", fontSize = 10.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = overview?.marketTrendStatus ?: "Memindai Pasar...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BullGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill(label = "Dipindai", value = "${overview?.totalScanned ?: 0} Aset", color = RadarCyan)
                        MetricPill(label = "Lolos Filter", value = "${overview?.totalPassedFilter ?: 0} Aset", color = AlertYellow)
                        MetricPill(label = "Sinyal Aktif", value = "${overview?.totalActiveSignals ?: 0} Sinyal", color = BullGreen)
                    }
                }
            }
        }

        // Quick Navigation Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedButton(
                    onClick = { onNavigateTab(MainTab.SCANNER) },
                    modifier = Modifier.weight(1f).testTag("quick_nav_scanner"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp), tint = RadarCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scanner", fontSize = 12.sp)
                }

                ElevatedButton(
                    onClick = { onNavigateTab(MainTab.SIGNALS) },
                    modifier = Modifier.weight(1f).testTag("quick_nav_signals"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = BullGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sinyal", fontSize = 12.sp)
                }

                ElevatedButton(
                    onClick = { onNavigateTab(MainTab.CALCULATOR) },
                    modifier = Modifier.weight(1f).testTag("quick_nav_calc"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp), tint = AlertYellow)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Risiko", fontSize = 12.sp)
                }
            }
        }

        // Section Title: Top 5 Kandidat Terbaik Hari Ini
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kandidat Terbaik Hari Ini",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Maksimal 5 aset teratas berdasarkan skor & reward/risk",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Top 5 Candidates List
        if (uiState.topCandidates.isEmpty()) {
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
                        Text(
                            text = "Tidak ada kandidat yang memenuhi kriteria saat ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pasar berada dalam filter ketat likuiditas & keamanan untuk menjaga modal Anda.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.topCandidates) { candidate ->
                TopCandidateCard(
                    candidate = candidate,
                    onSelect = {
                        onSelectAsset(candidate.ticker.symbol)
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
fun TopCandidateCard(
    candidate: ScannedAssetData,
    onSelect: () -> Unit
) {
    val ticker = candidate.ticker
    val signal = candidate.signal

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("candidate_card_${ticker.symbol}"),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Baris 1: Symbol & Score
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
                                fontWeight = FontWeight.ExtraBold,
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
                            text = "Vol 24h: $${String.format("%,.0f", ticker.volume24hUsd / 1_000_000.0)}M",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                ScoreBadge(score = candidate.scoreBreakdown.totalScore)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris 2: Harga, Change, Status Sinyal
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
                }
            }

            // Alasan Masuk Ranking
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Alasan Masuk Ranking:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RadarCyan
                    )
                    Text(
                        text = signal?.signalReason ?: "Memenuhi likuiditas & struktur teknikal positif.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = BullGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(ticker.securityStatus, fontSize = 10.sp, color = BullGreen)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lihat Chart & Posisi", fontSize = 11.sp, color = RadarCyan, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = RadarCyan, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
