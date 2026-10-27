package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalHistoryEntity
import com.example.data.model.WatchlistEntity
import com.example.ui.MainTab
import com.example.ui.ScoutUiState
import com.example.ui.components.ScoreBadge
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
fun WatchlistScreen(
    uiState: ScoutUiState,
    watchlist: List<WatchlistEntity>,
    signalHistory: List<SignalHistoryEntity>,
    onToggleWatchlist: (String, String) -> Unit,
    onSelectAsset: (String) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    onExportCsv: () -> String,
    onClearCsv: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Watchlist & Audit Sinyal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pantauan aset tersimpan dan riwayat audit lengkap seluruh sinyal sistem.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        val csv = onExportCsv()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Signal History CSV", csv)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "CSV berhasil disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = RadarCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_export_csv")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekspor CSV", fontSize = 11.sp)
                }
            }
        }

        // Subtabs: Watchlist vs Riwayat Sinyal
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = RadarCyan
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Watchlist (${watchlist.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Riwayat Sinyal (${signalHistory.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedSubTab == 0) {
            // TAB WATCHLIST
            if (watchlist.isEmpty()) {
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
                            Icon(Icons.Default.Star, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum Ada Aset di Watchlist", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tandai aset menarik di menu Pemindai atau Sinyal dengan ikon bookmark untuk menyimpannya di sini.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(watchlist) { item ->
                    val asset = uiState.scannedAssets.find { it.ticker.symbol == item.symbol }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectAsset(item.symbol)
                                onNavigateTab(MainTab.CALCULATOR)
                            },
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.baseAsset, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("/USDT", fontSize = 12.sp, color = TextSecondary)
                                }
                                val price = asset?.ticker?.lastPrice ?: 0.0
                                val change = asset?.ticker?.priceChangePercent ?: 0.0
                                Text(
                                    text = "$${String.format("%.2f", price)} (${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (change >= 0) BullGreen else BearRed
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                asset?.let { ScoreBadge(score = it.scoreBreakdown.totalScore) }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onToggleWatchlist(item.symbol, item.baseAsset) }
                                ) {
                                    Icon(Icons.Default.BookmarkRemove, contentDescription = "Hapus", tint = BearRed)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB RIWAYAT SINYAL
            if (signalHistory.isEmpty()) {
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
                            Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum Ada Riwayat Sinyal Tersimpan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Setiap sinyal yang dihasilkan saat pemindaian akan diaudit secara otomatis di sini.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(signalHistory) { hist ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(hist.createdAt))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp)),
                        color = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(hist.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(hist.setupType, fontSize = 11.sp, color = RadarCyan)
                                }
                                Text("Skor: ${hist.score}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Trigger: $${String.format("%.2f", hist.triggerPrice)}", fontSize = 10.sp, color = TextSecondary)
                                Text("SL: $${String.format("%.2f", hist.stopLoss)}", fontSize = 10.sp, color = BearRed)
                                Text("TP1: $${String.format("%.2f", hist.target1)}", fontSize = 10.sp, color = BullGreen)
                                Text("R:R: ${String.format("%.1f", hist.riskRewardRatio)}:1", fontSize = 10.sp, color = AlertYellow)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateStr, fontSize = 9.sp, color = TextMuted)
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                    color = if (hist.outcome == "TARGET_1_TERCAPAI" || hist.outcome == "TARGET_2_TERCAPAI") BullGreen.copy(alpha = 0.2f) else DarkSurfaceElevated
                                ) {
                                    Text(
                                        text = hist.outcome,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hist.outcome == "TARGET_1_TERCAPAI") BullGreen else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // CSV Viewer Dialog
    if (uiState.exportCsvContent != null) {
        AlertDialog(
            onDismissRequest = onClearCsv,
            title = { Text("Pratinjau Data CSV Sinyal", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Data telah disalin ke Clipboard. Anda dapat mengimpor format ini ke Excel atau Google Sheets:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = uiState.exportCsvContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = TextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onClearCsv) {
                    Text("Tutup")
                }
            },
            containerColor = DarkSurface
        )
    }
}
