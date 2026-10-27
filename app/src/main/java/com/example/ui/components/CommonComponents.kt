package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.SignalStatus
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
import kotlin.math.max
import kotlin.math.min

@Composable
fun ScoreBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val (color, label) = when {
        score >= 80 -> Pair(BullGreen, "Sangat Kuat")
        score >= 70 -> Pair(RadarCyan, "Potensial")
        score >= 50 -> Pair(AlertYellow, "Netral")
        else -> Pair(BearRed, "Lemah")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "SKOR",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color.copy(alpha = 0.8f)
                )
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun SignalStatusBadge(
    status: SignalStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderCol) = when (status) {
        SignalStatus.BUY_CANDIDATE -> Triple(BullGreen.copy(alpha = 0.15f), BullGreen, BullGreen.copy(alpha = 0.6f))
        SignalStatus.WAITING_CONFIRMATION -> Triple(AlertYellow.copy(alpha = 0.15f), AlertYellow, AlertYellow.copy(alpha = 0.6f))
        SignalStatus.WATCH -> Triple(RadarCyan.copy(alpha = 0.15f), RadarCyan, RadarCyan.copy(alpha = 0.6f))
        SignalStatus.INVALIDATED -> Triple(BearRed.copy(alpha = 0.15f), BearRed, BearRed.copy(alpha = 0.6f))
        SignalStatus.EXPIRED -> Triple(DarkSurfaceElevated, TextMuted, DarkSurfaceBorder)
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ApiStatusIndicator(
    statusText: String,
    modifier: Modifier = Modifier
) {
    val isOnline = statusText.contains("Online", ignoreCase = true)
    val indicatorColor = if (isOnline) BullGreen else AlertYellow

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(indicatorColor.copy(alpha = if (isOnline) alphaAnim else 1f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            fontSize = 11.sp,
            color = if (isOnline) TextPrimary else AlertYellow,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SystemDisclaimerBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF141926))
            .border(1.dp, Color(0xFF202A40), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Informasi",
                tint = RadarCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PEMBERITAHUAN: Sistem ini adalah alat analisis kuantitatif pasar kripto untuk riset mandiri. Bukan saran keuangan dan tidak melakukan eksekusi transaksi otomatis.",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Interactive Canvas-based Candlestick Chart dengan garis EMA 20 & EMA 50
 */
@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    supportLevel: Double = 0.0,
    resistanceLevel: Double = 0.0
) {
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }

    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Text("Memuat data grafik candlestick...", color = TextSecondary, fontSize = 12.sp)
        }
        return
    }

    val displayCandles = candles.takeLast(40)
    val maxHigh = displayCandles.maxOfOrNull { it.high } ?: 1.0
    val minLow = displayCandles.minOfOrNull { it.low } ?: 0.0
    val priceRange = max(maxHigh - minLow, 0.0001)

    val maxVolume = displayCandles.maxOfOrNull { it.volume } ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Header Chart Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Grafik Swing (1D)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp, 3.dp).background(RadarCyan))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("EMA20", fontSize = 10.sp, color = RadarCyan)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp, 3.dp).background(AlertYellow))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("EMA50", fontSize = 10.sp, color = AlertYellow)
                }
            }

            selectedCandleIndex?.let { idx ->
                if (idx in displayCandles.indices) {
                    val c = displayCandles[idx]
                    Text(
                        "C: $${String.format("%.2f", c.close)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (c.close >= c.open) BullGreen else BearRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas Area
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(displayCandles) {
                    detectTapGestures { offset ->
                        val barWidth = size.width / displayCandles.size
                        val index = (offset.x / barWidth).toInt().coerceIn(0, displayCandles.size - 1)
                        selectedCandleIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val chartHeight = height * 0.75f
            val volumeHeight = height * 0.22f
            val volumeTop = height - volumeHeight

            val candleCount = displayCandles.size
            val barWidth = width / candleCount
            val bodyWidth = max(barWidth * 0.65f, 2f)

            // Grid garis horizontal
            val gridSteps = 4
            for (g in 0..gridSteps) {
                val y = (chartHeight / gridSteps) * g
                drawLine(
                    color = Color(0xFF1B2438),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Gambar Candlestick & Volume
            for (i in displayCandles.indices) {
                val candle = displayCandles[i]
                val x = (i * barWidth) + (barWidth / 2f)

                val highY = chartHeight - (((candle.high - minLow) / priceRange) * chartHeight).toFloat()
                val lowY = chartHeight - (((candle.low - minLow) / priceRange) * chartHeight).toFloat()
                val openY = chartHeight - (((candle.open - minLow) / priceRange) * chartHeight).toFloat()
                val closeY = chartHeight - (((candle.close - minLow) / priceRange) * chartHeight).toFloat()

                val isBullish = candle.close >= candle.open
                val candleColor = if (isBullish) BullGreen else BearRed

                // Wick / Ekor lilin
                drawLine(
                    color = candleColor,
                    start = Offset(x, highY),
                    end = Offset(x, lowY),
                    strokeWidth = 1.5f
                )

                // Body lilin
                val bodyTop = min(openY, closeY)
                val bodyBottom = max(openY, closeY)
                val h = max(bodyBottom - bodyTop, 2f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(x - (bodyWidth / 2f), bodyTop),
                    size = Size(bodyWidth, h)
                )

                // Volume Bar
                val volRatio = (candle.volume / maxVolume).toFloat().coerceIn(0.05f, 1f)
                val barH = volumeHeight * volRatio
                drawRect(
                    color = candleColor.copy(alpha = 0.35f),
                    topLeft = Offset(x - (bodyWidth / 2f), height - barH),
                    size = Size(bodyWidth, barH)
                )
            }

            // Garis Support (Hijau tipis putus-putus) jika dalam range
            if (supportLevel in minLow..maxHigh) {
                val supY = chartHeight - (((supportLevel - minLow) / priceRange) * chartHeight).toFloat()
                drawLine(
                    color = BullGreen.copy(alpha = 0.7f),
                    start = Offset(0f, supY),
                    end = Offset(width, supY),
                    strokeWidth = 1.2f
                )
            }

            // Garis Resistensi (Merah tipis putus-putus) jika dalam range
            if (resistanceLevel in minLow..maxHigh) {
                val resY = chartHeight - (((resistanceLevel - minLow) / priceRange) * chartHeight).toFloat()
                drawLine(
                    color = BearRed.copy(alpha = 0.7f),
                    start = Offset(0f, resY),
                    end = Offset(width, resY),
                    strokeWidth = 1.2f
                )
            }
        }
    }
}
