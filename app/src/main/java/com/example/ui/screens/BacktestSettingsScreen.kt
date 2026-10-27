package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BacktestResult
import com.example.data.model.FilterSettings
import com.example.ui.ScoutUiState
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

@Composable
fun BacktestSettingsScreen(
    uiState: ScoutUiState,
    onRunBacktest: () -> Unit,
    onUpdateFilterSettings: (FilterSettings) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }
    val result = uiState.backtestResult
    val asset = uiState.selectedAsset

    // Settings local edit state
    var minVolText by remember(uiState.filterSettings.min24hVolumeUsd) {
        mutableStateOf(String.format("%.0f", uiState.filterSettings.min24hVolumeUsd))
    }
    var minDaysText by remember(uiState.filterSettings.minHistoricalDays) {
        mutableStateOf(uiState.filterSettings.minHistoricalDays.toString())
    }
    var minScoreText by remember(uiState.filterSettings.minScoreToSignal) {
        mutableStateOf(uiState.filterSettings.minScoreToSignal.toString())
    }
    var minRRText by remember(uiState.filterSettings.minRewardToRisk) {
        mutableStateOf(String.format("%.1f", uiState.filterSettings.minRewardToRisk))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Evaluasi Strategi & Pengaturan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Backtest historis matematis tanpa look-ahead bias dan kustomisasi parameter filter.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Subtabs: Backtest vs Pengaturan
        item {
            TabRow(
                selectedTabIndex = subTab,
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[subTab]),
                        color = BullGreen
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    text = { Text("Simulasi Backtest", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    text = { Text("Pengaturan Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (subTab == 0) {
            // TAB BACKTEST
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
                            Column {
                                Text(
                                    text = "Aset Teruji: ${asset?.ticker?.symbol ?: "Pilih Aset"}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Menguji candle tertutup harian (1D) dengan pengurangan fee 0.1% + slippage.",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Button(
                                onClick = onRunBacktest,
                                enabled = !uiState.isBacktesting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BullGreen,
                                    contentColor = Color(0xFF00391A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_run_backtest")
                            ) {
                                if (uiState.isBacktesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkBg, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Uji Strategi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (result != null) {
                // Statistik Kartu Metrik
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BullGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        color = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(result.strategyName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RadarCyan)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BacktestMetricBox("Win Rate", "${String.format("%.1f", result.winRatePercent)}%", if (result.winRatePercent >= 50) BullGreen else AlertYellow)
                                BacktestMetricBox("Total Return (Net)", "${String.format("%.2f", result.totalReturnPercent)}%", if (result.totalReturnPercent >= 0) BullGreen else BearRed)
                                BacktestMetricBox("Profit Factor", String.format("%.2f", result.profitFactor), if (result.profitFactor >= 1.5) BullGreen else AlertYellow)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BacktestMetricBox("Total Transaksi", "${result.totalTrades} Trade", TextPrimary)
                                BacktestMetricBox("Max Drawdown", "-${String.format("%.1f", result.maxDrawdownPercent)}%", BearRed)
                                BacktestMetricBox("Expectancy / Trade", "${String.format("%.2f", result.expectancyPerTrade)}%", TextSecondary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BacktestMetricBox("Rata-rata Menang", "+${String.format("%.2f", result.avgWinPercent)}%", BullGreen)
                                BacktestMetricBox("Rata-rata Kalah", "${String.format("%.2f", result.avgLossPercent)}%", BearRed)
                                BacktestMetricBox("Return Kotor (Gross)", "+${String.format("%.2f", result.returnBeforeFeesPercent)}%", TextMuted)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Riwayat Eksekusi Simulasi (${result.trades.size} Transaksi):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(result.trades) { trade ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp)),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(trade.tradeId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(trade.setup, fontSize = 10.sp, color = RadarCyan)
                                }
                                Text(
                                    text = "Entri: $${String.format("%.2f", trade.entryPrice)} → Keluar: $${String.format("%.2f", trade.exitPrice)} (${trade.exitReason})",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (trade.returnPercent >= 0) "+" else ""}${String.format("%.2f", trade.returnPercent)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (trade.isWin) BullGreen else BearRed
                                )
                                Text("Fee: -${String.format("%.2f", trade.feeDeductionPercent)}%", fontSize = 9.sp, color = TextMuted)
                            }
                        }
                    }
                }
            } else {
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
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Klik 'Uji Strategi' untuk Menjalankan Backtest", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Algoritma akan mensimulasikan aturan swing trading pada candle historis tanpa look-ahead bias.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // TAB PENGATURAN FILTER
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    color = DarkSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Parameter Ambang Batas Pemindai", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RadarCyan)

                        // Min 24h Volume USD
                        Text("Volume Spot 24 Jam Minimal (USD):", fontSize = 11.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = minVolText,
                            onValueChange = { minVolText = it },
                            modifier = Modifier.fillMaxWidth().testTag("input_min_volume"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = BullGreen,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Min Historical Days
                        Text("Usia Candle Historis Minimal (Hari):", fontSize = 11.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = minDaysText,
                            onValueChange = { minDaysText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = BullGreen,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Min Score
                        Text("Skor Minimum untuk Kandidat Beli (0-100):", fontSize = 11.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = minScoreText,
                            onValueChange = { minScoreText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = BullGreen,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Min Reward/Risk
                        Text("Minimal Reward-to-Risk Rasio (R:R):", fontSize = 11.sp, color = TextSecondary)
                        OutlinedTextField(
                            value = minRRText,
                            onValueChange = { minRRText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = BullGreen,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val updated = uiState.filterSettings.copy(
                                    min24hVolumeUsd = minVolText.toDoubleOrNull() ?: 2_000_000.0,
                                    minHistoricalDays = minDaysText.toIntOrNull() ?: 180,
                                    minScoreToSignal = minScoreText.toIntOrNull() ?: 75,
                                    minRewardToRisk = minRRText.toDoubleOrNull() ?: 2.0
                                )
                                onUpdateFilterSettings(updated)
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_save_settings"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BullGreen,
                                contentColor = Color(0xFF00391A)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan & Perbarui Pemindai", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BacktestMetricBox(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(title, fontSize = 9.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
