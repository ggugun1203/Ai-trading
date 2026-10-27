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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WatchlistEntity
import com.example.ui.ScoutUiState
import com.example.ui.components.CandlestickChart
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

@Composable
fun CalculatorChartScreen(
    uiState: ScoutUiState,
    watchlist: List<WatchlistEntity>,
    onToggleWatchlist: (String, String) -> Unit,
    onUpdateRiskParameters: (capitalIdr: Double, riskPercent: Double, usdRate: Double) -> Unit,
    onUpdateCustomPosition: (entryPrice: Double, stopLoss: Double) -> Unit
) {
    val asset = uiState.selectedAsset
    val calc = uiState.positionCalculation
    val indicators = asset?.indicators

    var capitalText by remember(uiState.riskSettings.capitalIdr) {
        mutableStateOf(String.format("%.0f", uiState.riskSettings.capitalIdr))
    }
    var riskSliderValue by remember(uiState.riskSettings.riskPerTradePercent) {
        mutableStateOf(uiState.riskSettings.riskPerTradePercent.toFloat())
    }

    val defaultEntry = asset?.signal?.triggerPrice ?: asset?.ticker?.lastPrice ?: 100.0
    val defaultSl = asset?.signal?.stopLoss ?: (defaultEntry * 0.96)

    var entryText by remember(defaultEntry) {
        mutableStateOf(String.format("%.4f", defaultEntry))
    }
    var stopLossText by remember(defaultSl) {
        mutableStateOf(String.format("%.4f", defaultSl))
    }

    val isSaved = watchlist.any { it.symbol == asset?.ticker?.symbol }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Aset Terpilih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = asset?.ticker?.baseAsset ?: "Pilih Aset",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "/USDT",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Harga: $${String.format("%.4f", asset?.ticker?.lastPrice ?: 0.0)} • 24h: ${if ((asset?.ticker?.priceChangePercent ?: 0.0) >= 0) "+" else ""}${String.format("%.2f", asset?.ticker?.priceChangePercent ?: 0.0)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if ((asset?.ticker?.priceChangePercent ?: 0.0) >= 0) BullGreen else BearRed
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (asset != null) {
                        IconButton(
                            onClick = { onToggleWatchlist(asset.ticker.symbol, asset.ticker.baseAsset) },
                            modifier = Modifier.testTag("btn_chart_watchlist")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isSaved) AlertYellow else TextSecondary
                            )
                        }
                        ScoreBadge(score = asset.scoreBreakdown.totalScore)
                    }
                }
            }
        }

        // Candlestick Chart Component
        item {
            CandlestickChart(
                candles = uiState.selectedCandles,
                supportLevel = indicators?.supportLevel ?: 0.0,
                resistanceLevel = indicators?.resistanceLevel ?: 0.0
            )
        }

        // Indikator Ringkas
        if (indicators != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
                    color = DarkSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Parameter Indikator Teknikal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RadarCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            IndicatorChip("RSI 14", String.format("%.1f", indicators.rsi14), if (indicators.rsi14 in 50.0..68.0) BullGreen else AlertYellow)
                            IndicatorChip("MACD Hist", String.format("%.3f", indicators.macdHist), if (indicators.macdHist > 0) BullGreen else BearRed)
                            IndicatorChip("ATR 14", String.format("%.3f", indicators.atr14), TextPrimary)
                            IndicatorChip("RVol", "${String.format("%.1f", indicators.relativeVolume)}x", if (indicators.relativeVolume >= 1.2) BullGreen else TextSecondary)
                        }
                    }
                }
            }
        }

        // Kalkulator Risiko & Ukuran Posisi
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kalkulator Manajemen Risiko Spot",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Spot murni tanpa leverage, alokasi berbasis batasan risiko modal.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = BullGreen)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Modal dalam IDR
                    Text("Modal Trading (IDR):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = capitalText,
                        onValueChange = {
                            capitalText = it
                            val cap = it.toDoubleOrNull() ?: 3_000_000.0
                            onUpdateRiskParameters(cap, riskSliderValue.toDouble(), uiState.riskSettings.usdToIdrRate)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_capital_idr"),
                        leadingIcon = { Text("Rp", fontWeight = FontWeight.Bold, color = RadarCyan, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider Risiko per Posisi (%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Risiko per Posisi:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${String.format("%.2f", riskSliderValue)}% (Maks 1.0%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (riskSliderValue <= 0.8f) BullGreen else AlertYellow
                        )
                    }
                    Slider(
                        value = riskSliderValue,
                        onValueChange = {
                            riskSliderValue = it
                            val cap = capitalText.toDoubleOrNull() ?: 3_000_000.0
                            onUpdateRiskParameters(cap, it.toDouble(), uiState.riskSettings.usdToIdrRate)
                        },
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = BullGreen,
                            activeTrackColor = BullGreen,
                            inactiveTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.testTag("slider_risk_percent")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Entry & Stop Loss Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Harga Entri ($):", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = entryText,
                                onValueChange = {
                                    entryText = it
                                    val e = it.toDoubleOrNull() ?: defaultEntry
                                    val s = stopLossText.toDoubleOrNull() ?: defaultSl
                                    onUpdateCustomPosition(e, s)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("input_entry_price"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated,
                                    focusedBorderColor = RadarCyan,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stop Loss ($):", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = stopLossText,
                                onValueChange = {
                                    stopLossText = it
                                    val e = entryText.toDoubleOrNull() ?: defaultEntry
                                    val s = it.toDoubleOrNull() ?: defaultSl
                                    onUpdateCustomPosition(e, s)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("input_stop_loss"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated,
                                    focusedBorderColor = BearRed,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hasil Kalkulasi Posisi
                    if (calc != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (calc.isValid) BullGreen.copy(alpha = 0.5f) else BearRed.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                ),
                            color = DarkSurfaceElevated
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Batas Risiko Moneter:", fontSize = 11.sp, color = TextSecondary)
                                    Text("Rp${String.format("%,.0f", calc.riskAmountIdr)} ($${String.format("%.2f", calc.riskAmountUsd)})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AlertYellow)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Risiko per Unit:", fontSize = 11.sp, color = TextSecondary)
                                    Text("$${String.format("%.4f", calc.riskPerUnitUsd)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Jumlah Pembelian (Unit):", fontSize = 11.sp, color = TextSecondary)
                                    Text("${String.format("%.4f", calc.adjustedQuantity)} ${asset?.ticker?.baseAsset ?: ""}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BullGreen)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Nilai Total Posisi:", fontSize = 11.sp, color = TextSecondary)
                                    Text("Rp${String.format("%,.0f", calc.positionValueIdr)} ($${String.format("%.2f", calc.positionValueUsd)})", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = RadarCyan)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Estimasi Biaya & Slippage:", fontSize = 10.sp, color = TextMuted)
                                    Text("~$${String.format("%.2f", calc.estimatedFeeUsd)}", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        // Peringatan jika melebihi batas atau tidak valid
                        if (calc.warningMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (calc.isValid) AlertYellow.copy(alpha = 0.12f) else BearRed.copy(alpha = 0.12f))
                                    .border(1.dp, if (calc.isValid) AlertYellow.copy(alpha = 0.5f) else BearRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = if (calc.isValid) Icons.Default.Info else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (calc.isValid) AlertYellow else BearRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = calc.warningMessage,
                                        fontSize = 10.sp,
                                        color = if (calc.isValid) AlertYellow else BearRed,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CATATAN RESMI: Stop-loss tidak menjamin harga eksekusi yang tepat pada exchange karena faktor likuiditas pasar, gap harga, dan slippage jaringan.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IndicatorChip(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
