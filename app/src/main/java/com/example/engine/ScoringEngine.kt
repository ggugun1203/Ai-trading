package com.example.engine

import com.example.data.model.IndicatorValues
import com.example.data.model.ScoreBreakdown
import kotlin.math.abs
import kotlin.math.roundToInt

object ScoringEngine {

    /**
     * Hitung Skor Peluang Swing Trading berdasarkan 5 pilar matematis
     * score = 0.30 * trend + 0.20 * momentum + 0.20 * volume + 0.20 * setup + 0.10 * risk
     */
    fun calculateScore(
        price: Double,
        indicators: IndicatorValues,
        volume24hUsd: Double,
        spreadPercent: Double = 0.05
    ): ScoreBreakdown {
        // 1. Kekuatan Tren (Trend Score: 0 - 100)
        var trendScore = 0.0
        val isBullishStacked = price > indicators.ema20 && indicators.ema20 > indicators.ema50 && indicators.ema50 > indicators.ema200
        val isAboveEma50And200 = price > indicators.ema50 && indicators.ema50 > indicators.ema200
        val isAboveEma50 = price > indicators.ema50

        trendScore += when {
            isBullishStacked -> 50.0
            isAboveEma50And200 -> 38.0
            isAboveEma50 -> 22.0
            else -> 5.0
        }

        // Kemiringan / Slope: EMA20 di atas EMA50
        if (indicators.ema20 > indicators.ema50) {
            trendScore += 20.0
        }

        // Kedekatan harga dengan EMA20/50 (tidak over-extended)
        val distEma50Percent = if (indicators.ema50 > 0) ((price - indicators.ema50) / indicators.ema50) * 100.0 else 0.0
        trendScore += when {
            distEma50Percent in 0.5..6.0 -> 30.0  // Zona pullback ideal
            distEma50Percent in 6.0..12.0 -> 20.0 // Tren masih berlanjut
            distEma50Percent > 12.0 -> 10.0      // Agak over-extended
            else -> 0.0
        }
        trendScore = trendScore.coerceIn(0.0, 100.0)

        // 2. Momentum (Momentum Score: 0 - 100)
        var momentumScore = 0.0
        // RSI Sweet spot 50 - 68
        momentumScore += when {
            indicators.rsi14 in 52.0..66.0 -> 40.0 // Zona akselerasi bullish optimal
            indicators.rsi14 in 48.0..72.0 -> 25.0
            indicators.rsi14 > 72.0 -> 8.0         // Overbought
            else -> 5.0                            // Momentum melemah / oversold
        }

        // MACD Bullish Crossover & Positif
        if (indicators.macdLine > indicators.macdSignal) {
            momentumScore += 25.0
            if (indicators.macdHist > 0) {
                momentumScore += 15.0 // Histogram hijau / konfirmasi pembeli
            }
        }

        // Momentum 10-candle positif
        if (indicators.momentumPercent > 0) {
            val pts = (indicators.momentumPercent * 4.0).coerceIn(0.0, 20.0)
            momentumScore += pts
        }
        momentumScore = momentumScore.coerceIn(0.0, 100.0)

        // 3. Volume (Volume Score: 0 - 100)
        var volumeScore = 0.0
        // Relative Volume (RVol)
        volumeScore += when {
            indicators.relativeVolume >= 2.0 -> 50.0
            indicators.relativeVolume >= 1.4 -> 40.0
            indicators.relativeVolume >= 1.1 -> 30.0
            indicators.relativeVolume >= 0.8 -> 15.0
            else -> 5.0
        }

        // Likuiditas Volume Spot 24 Jam
        volumeScore += when {
            volume24hUsd >= 50_000_000.0 -> 30.0
            volume24hUsd >= 10_000_000.0 -> 25.0
            volume24hUsd >= 2_000_000.0 -> 15.0
            else -> 0.0
        }

        // Konfirmasi candle hijau dengan volume di atas rata-rata
        if (indicators.priceChange24h > 0 && indicators.relativeVolume >= 1.0) {
            volumeScore += 20.0
        }
        volumeScore = volumeScore.coerceIn(0.0, 100.0)

        // 4. Setup Teknikal (Setup Score: 0 - 100)
        var setupScore = 0.0
        val distToResistance = if (indicators.resistanceLevel > 0) {
            ((indicators.resistanceLevel - price) / price) * 100.0
        } else 100.0

        val distToSupport = if (price > 0 && indicators.supportLevel > 0) {
            ((price - indicators.supportLevel) / price) * 100.0
        } else 100.0

        when {
            // Setup Breakout: Harga mendekati atau menembus resistensi dengan RVol tinggi
            distToResistance in -0.5..2.5 && indicators.relativeVolume >= 1.2 -> {
                setupScore = 90.0
                if (indicators.rsi14 in 55.0..68.0) setupScore += 10.0
            }
            // Setup Pullback: Harga dekat EMA20 atau EMA50 dan di atas support
            distEma50Percent in 0.2..4.5 && isAboveEma50And200 -> {
                setupScore = 85.0
                if (indicators.macdLine > indicators.macdSignal) setupScore += 10.0
            }
            // Konsolidasi dalam tren naik
            isAboveEma50And200 -> {
                setupScore = 65.0
            }
            else -> {
                setupScore = 30.0
            }
        }
        setupScore = setupScore.coerceIn(0.0, 100.0)

        // 5. Kondisi Risiko (Risk Score: 0 - 100)
        var riskScore = 0.0
        // Jarak ke Stop Loss / Support yang rasional (antara 2% - 6% dari harga)
        riskScore += when {
            distToSupport in 2.0..6.0 -> 50.0
            distToSupport in 1.0..8.0 -> 30.0
            else -> 10.0
        }

        // Volatilitas ATR normal (2% - 5% dari harga)
        val atrPercent = if (price > 0) (indicators.atr14 / price) * 100.0 else 0.0
        riskScore += when {
            atrPercent in 1.5..5.5 -> 30.0
            atrPercent in 0.5..8.0 -> 20.0
            else -> 5.0
        }

        // Spread tipis
        riskScore += when {
            spreadPercent <= 0.1 -> 20.0
            spreadPercent <= 0.3 -> 10.0
            else -> 0.0
        }
        riskScore = riskScore.coerceIn(0.0, 100.0)

        // Total gabungan dengan bobot matematis:
        val total = (0.30 * trendScore) +
                    (0.20 * momentumScore) +
                    (0.20 * volumeScore) +
                    (0.20 * setupScore) +
                    (0.10 * riskScore)

        return ScoreBreakdown(
            trendScore = trendScore,
            momentumScore = momentumScore,
            volumeScore = volumeScore,
            setupScore = setupScore,
            riskScore = riskScore,
            totalScore = total.roundToInt().coerceIn(0, 100)
        )
    }
}
