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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import com.example.data.model.SwingSignal
import com.example.ui.MainTab
import com.example.ui.ScoutUiState
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
fun SignalDetailScreen(
    uiState: ScoutUiState,
    onSelectAsset: (String) -> Unit,
    onNavigateTab: (MainTab) -> Unit
) {
    // Filter semua aset yang memiliki sinyal (BUY_CANDIDATE, WAITING_CONFIRMATION, WATCH)
    val signals = uiState.scannedAssets.mapNotNull { it.signal }
        .sortedWith(
            compareByDescending<SwingSignal> { it.status == SignalStatus.BUY_CANDIDATE }
                .thenByDescending { it.score }
                .thenByDescending { it.riskRewardRatio }
        )

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
                text = "Sinyal Swing Trading Aktif",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Setup Breakout terkonfirmasi & Pullback tren naik (1D + konfirmasi 4H).",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        item {
            SystemDisclaimerBanner()
        }

        if (signals.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    color = DarkSurface
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tidak Ada Sinyal yang Memenuhi Kriteria",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sistem menerapkan ambang batas ketat (Skor minimal ${uiState.filterSettings.minScoreToSignal} & R:R minimal ${uiState.filterSettings.minRewardToRisk}:1) untuk menghindari sinyal palsu saat pasar berkonsolidasi.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(signals) { signal ->
                SignalFullCard(
                    signal = signal,
                    onCalculate = {
                        onSelectAsset(signal.symbol)
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
fun SignalFullCard(
    signal: SwingSignal,
    onCalculate: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val createdStr = dateFormat.format(Date(signal.timestamp))
    val expiryStr = dateFormat.format(Date(signal.expiryTimestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (signal.status == SignalStatus.BUY_CANDIDATE) BullGreen.copy(alpha = 0.5f) else DarkSurfaceBorder,
                RoundedCornerShape(14.dp)
            )
            .testTag("signal_card_${signal.symbol}"),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = signal.symbol,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = signal.exchange,
                            fontSize = 10.sp,
                            color = RadarCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Setup: ${signal.setupType.title}",
                        fontSize = 11.sp,
                        color = AlertYellow,
                        fontWeight = FontWeight.Medium
                    )
                }

                ScoreBadge(score = signal.score)
            }

            Spacer(modifier = Modifier.height(10.dp))
            SignalStatusBadge(status = signal.status)

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Metrik Level Harga
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SignalMetricRow("Harga Referensi", formatPrice(signal.referencePrice), TextSecondary)
                        SignalMetricRow("Harga Pemicu (Trigger)", formatPrice(signal.triggerPrice), RadarCyan)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SignalMetricRow("Rentang Entri", "${formatPrice(signal.entryMin)} - ${formatPrice(signal.entryMax)}", TextPrimary)
                        SignalMetricRow("Stop Loss (SL)", formatPrice(signal.stopLoss), BearRed)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SignalMetricRow("Target 1 (1:2 R:R)", formatPrice(signal.target1), BullGreen)
                        SignalMetricRow("Target 2 (Runner)", formatPrice(signal.target2), BullGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SignalMetricRow("Reward / Risk (R:R)", "${String.format("%.2f", signal.riskRewardRatio)}:1", RadarCyan)
                        SignalMetricRow("Kedaluwarsa", expiryStr, TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rationale & Risiko
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Alasan Sinyal:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Text(
                    text = signal.signalReason,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Risiko Utama & Kondisi Pembatalan:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertYellow
                )
                Text(
                    text = "${signal.primaryRisks} ${signal.invalidationCondition}",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_calc_position_${signal.symbol}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BullGreen,
                    contentColor = Color(0xFF00391A)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hitung Ukuran Posisi Aset Ini",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SignalMetricRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1.0) String.format("$%.2f", price) else String.format("$%.4f", price)
}
