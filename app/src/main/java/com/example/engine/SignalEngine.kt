package com.example.engine

import com.example.data.model.Candle
import com.example.data.model.FilterSettings
import com.example.data.model.IndicatorValues
import com.example.data.model.ScoreBreakdown
import com.example.data.model.SetupType
import com.example.data.model.SignalStatus
import com.example.data.model.SwingSignal
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

object SignalEngine {

    /**
     * Evaluasi sinyal swing trading berdasarkan candle 1D dan konfirmasi 4H
     */
    fun evaluateSignal(
        symbol: String,
        dailyCandles: List<Candle>,
        fourHourCandles: List<Candle> = emptyList(),
        indicators: IndicatorValues,
        scoreBreakdown: ScoreBreakdown,
        filterSettings: FilterSettings
    ): SwingSignal? {
        val currentPrice = dailyCandles.lastOrNull()?.close ?: return null
        val score = scoreBreakdown.totalScore

        // Kriteria dasar tren naik
        val isUptrend = currentPrice > indicators.ema50 && indicators.ema50 >= (indicators.ema200 * 0.98)
        val isRsiValid = indicators.rsi14 in 48.0..70.0

        // Tentukan Setup (Breakout vs Pullback)
        val setupType: SetupType
        val triggerPrice: Double
        val entryMin: Double
        val entryMax: Double
        val stopLoss: Double
        val signalReason: String

        val distToResistance = if (indicators.resistanceLevel > 0) {
            ((indicators.resistanceLevel - currentPrice) / currentPrice) * 100.0
        } else 100.0

        val distEma50 = if (indicators.ema50 > 0) {
            ((currentPrice - indicators.ema50) / indicators.ema50) * 100.0
        } else 0.0

        if (distToResistance in -0.8..2.2 && indicators.relativeVolume >= 1.15) {
            // Setup 1: Breakout Terkonfirmasi
            setupType = SetupType.BREAKOUT
            triggerPrice = max(indicators.resistanceLevel, currentPrice * 1.002)
            entryMin = currentPrice * 0.998
            entryMax = triggerPrice * 1.01
            // Stop loss di bawah support lokal atau di bawah EMA20
            val slCandidate = max(indicators.supportLevel, indicators.ema20 * 0.985)
            stopLoss = if (slCandidate >= triggerPrice * 0.99) triggerPrice * 0.96 else slCandidate
            signalReason = "Penembusan resistensi $symbol pada level ${formatPrice(indicators.resistanceLevel)} didukung lonjakan Relative Volume ${String.format("%.1f", indicators.relativeVolume)}x dan struktur tren naik."
        } else if (distEma50 in 0.2..5.5 && isUptrend) {
            // Setup 2: Pullback pada Tren Naik
            setupType = SetupType.PULLBACK
            triggerPrice = currentPrice
            entryMin = currentPrice * 0.992
            entryMax = currentPrice * 1.008
            // Stop loss di bawah EMA50 / Support terdekat
            val slCandidate = minOf(indicators.ema50 * 0.98, indicators.supportLevel)
            stopLoss = if (slCandidate >= triggerPrice * 0.99) triggerPrice * 0.955 else slCandidate
            signalReason = "Peluang beli saat pullback ke zona support dinamis EMA 50 (${formatPrice(indicators.ema50)}) dengan RSI ${String.format("%.1f", indicators.rsi14)} yang mendingin tanpa merusak struktur tren."
        } else {
            // Setup belum matang untuk swing trading terdefinisi
            setupType = SetupType.PULLBACK
            triggerPrice = currentPrice
            entryMin = currentPrice * 0.99
            entryMax = currentPrice * 1.01
            stopLoss = currentPrice * 0.95
            signalReason = "Sedang dalam observasi konsolidasi harga."
        }

        val riskPerUnit = abs(triggerPrice - stopLoss)
        if (riskPerUnit <= 0.0) return null

        // Target 1: minimal 2x Risk (1:2 R:R)
        val target1 = triggerPrice + (riskPerUnit * 2.0)
        // Target 2: 3.5x Risk (1:3.5 R:R)
        val target2 = triggerPrice + (riskPerUnit * 3.5)

        val riskReward = (target1 - triggerPrice) / riskPerUnit

        // Evaluasi konfirmasi 4H jika data tersedia
        var fourHourInvalidates = false
        if (fourHourCandles.size >= 14) {
            val last4hCloses = fourHourCandles.map { it.close }
            val last4hRsi = TechnicalIndicatorEngine.calculateRSI(last4hCloses, 14).lastOrNull() ?: 50.0
            // Jika timeframe 4H anjlok di bawah RSI 32 atau crash tajam, batalkan
            if (last4hRsi < 32.0) {
                fourHourInvalidates = true
            }
        }

        // Tentukan Status Sinyal
        val status: SignalStatus = when {
            fourHourInvalidates -> SignalStatus.INVALIDATED
            score >= filterSettings.minScoreToSignal && riskReward >= filterSettings.minRewardToRisk && isUptrend && isRsiValid -> {
                SignalStatus.BUY_CANDIDATE
            }
            score in (filterSettings.minScoreToSignal - 10) until filterSettings.minScoreToSignal && isUptrend -> {
                SignalStatus.WAITING_CONFIRMATION
            }
            isUptrend -> {
                SignalStatus.WATCH
            }
            else -> {
                SignalStatus.INVALIDATED
            }
        }

        val primaryRisks = "Volatilitas aset kripto, risiko breakdown support di bawah ${formatPrice(stopLoss)}, serta kemungkinan pelemahan mendadak indeks pasar BTC."
        val invalidationCondition = "Harga penutupan harian (1D close) jatuh di bawah ${formatPrice(stopLoss)} atau lonjakan volume jual saat breakdown."
        val now = System.currentTimeMillis()
        val expiryDays = if (setupType == SetupType.BREAKOUT) 3 else 7
        val expiry = now + (expiryDays * 24L * 60L * 60L * 1000L)

        return SwingSignal(
            id = "SIG_${symbol}_${now}",
            symbol = symbol,
            exchange = "Binance Spot",
            timestamp = now,
            setupType = setupType,
            referencePrice = currentPrice,
            triggerPrice = triggerPrice,
            entryMin = entryMin,
            entryMax = entryMax,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            score = score,
            riskRewardRatio = riskReward,
            status = status,
            signalReason = signalReason,
            primaryRisks = primaryRisks,
            invalidationCondition = invalidationCondition,
            expiryTimestamp = expiry,
            outcomeStatus = if (status == SignalStatus.BUY_CANDIDATE) "AKTIF" else "PANTAU"
        )
    }

    private fun formatPrice(price: Double): String {
        return if (price >= 1.0) String.format("$%.2f", price) else String.format("$%.4f", price)
    }
}
