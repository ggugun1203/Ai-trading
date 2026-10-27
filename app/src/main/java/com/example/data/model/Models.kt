package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representasi candlestick pasar (KLine)
 */
data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val isClosed: Boolean = true
)

/**
 * Data Ticker 24 Jam dari Spot Market
 */
data class MarketTicker(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String = "USDT",
    val lastPrice: Double,
    val priceChangePercent: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24hUsd: Double,
    val countTrades: Long = 0,
    val isAnomaly: Boolean = false,
    val securityStatus: String = "Terverifikasi", // "Terverifikasi", "Audited", "Belum terverifikasi"
    val daysHistory: Int = 240,
    val marketCapUsd: Double = 0.0
)

/**
 * Hasil perhitungan indikator teknikal
 */
data class IndicatorValues(
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val rsi14: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHist: Double,
    val atr14: Double,
    val volumeSma20: Double,
    val relativeVolume: Double,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val priceChange24h: Double,
    val momentumPercent: Double
)

/**
 * Rincian Skor Peluang (0-100)
 * score = 0.30 * trend + 0.20 * momentum + 0.20 * volume + 0.20 * setup + 0.10 * risk
 */
data class ScoreBreakdown(
    val trendScore: Double,     // bobot 30%
    val momentumScore: Double,  // bobot 20%
    val volumeScore: Double,    // bobot 20%
    val setupScore: Double,     // bobot 20%
    val riskScore: Double,      // bobot 10%
    val totalScore: Int         // 0 - 100
)

/**
 * Status Sinyal Swing Trading
 */
enum class SignalStatus(val label: String) {
    BUY_CANDIDATE("Kandidat Beli (Buy Candidate)"),
    WAITING_CONFIRMATION("Menunggu Konfirmasi"),
    WATCH("Pantau (Watch)"),
    INVALIDATED("Dibatalkan (Invalidated)"),
    EXPIRED("Kedaluwarsa (Expired)")
}

/**
 * Tipe Setup Swing Trading
 */
enum class SetupType(val title: String) {
    BREAKOUT("Breakout Terkonfirmasi"),
    PULLBACK("Pullback pada Tren Naik")
}

/**
 * Sinyal Swing Trading Terstruktur
 */
data class SwingSignal(
    val id: String,
    val symbol: String,
    val exchange: String = "Binance Spot",
    val timestamp: Long,
    val setupType: SetupType,
    val referencePrice: Double,
    val triggerPrice: Double,
    val entryMin: Double,
    val entryMax: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val score: Int,
    val riskRewardRatio: Double,
    val status: SignalStatus,
    val signalReason: String,
    val primaryRisks: String,
    val invalidationCondition: String,
    val expiryTimestamp: Long,
    val outcomeStatus: String = "AKTIF" // "AKTIF", "TARGET_1_TERCAPAI", "TARGET_2_TERCAPAI", "STOP_LOSS_TERKENA", "KADALUWARSA"
)

/**
 * Hasil filter likuiditas dan keamanan
 */
data class FilterResult(
    val symbol: String,
    val isPassed: Boolean,
    val failureReasons: List<String> = emptyList(),
    val historicalDays: Int,
    val volume24hUsd: Double,
    val median30dVolumeUsd: Double,
    val marketCapUsd: Double,
    val spreadPercent: Double,
    val isAnomaly: Boolean
)

/**
 * Filter konfigurasi pengguna
 */
data class FilterSettings(
    val minHistoricalDays: Int = 180,
    val minMarketCapUsd: Double = 50_000_000.0,
    val min24hVolumeUsd: Double = 2_000_000.0,
    val minMedian30dVolumeUsd: Double = 1_000_000.0,
    val minScoreToSignal: Int = 75,
    val minRewardToRisk: Double = 2.0,
    val maxSpreadPercent: Double = 0.5
)

/**
 * Konfigurasi Manajemen Risiko (Risk Management)
 */
data class RiskSettings(
    val capitalIdr: Double = 3_000_000.0,       // Default Rp3.000.000
    val riskPerTradePercent: Double = 0.5,      // Default 0.5%, maks 1%
    val maxPositions: Int = 3,                  // Maksimal 3 posisi terbuka
    val combinedRiskLimitPercent: Double = 2.0, // Batas risiko gabungan 2%
    val usdToIdrRate: Double = 16_000.0,        // Nilai tukar USD/IDR
    val feePercent: Double = 0.1,               // 0.1% fee
    val slippagePercent: Double = 0.05          // 0.05% estimasi slippage
)

/**
 * Rincian Ukuran Posisi yang Dihitung
 */
data class PositionCalculation(
    val capitalIdr: Double,
    val riskPercent: Double,
    val riskAmountIdr: Double,
    val riskAmountUsd: Double,
    val entryPriceUsd: Double,
    val stopLossUsd: Double,
    val riskPerUnitUsd: Double,
    val rawQuantity: Double,
    val adjustedQuantity: Double,
    val positionValueUsd: Double,
    val positionValueIdr: Double,
    val estimatedFeeUsd: Double,
    val isValid: Boolean,
    val warningMessage: String? = null
)

/**
 * Ringkasan Pasar (Overview)
 */
data class MarketOverview(
    val btcPrice: Double,
    val btcChange24h: Double,
    val ethPrice: Double,
    val ethChange24h: Double,
    val marketTrendStatus: String, // "Bullish Kuat", "Bullish", "Sideways / Netral", "Koreksi / Bearish"
    val totalScanned: Int,
    val totalPassedFilter: Int,
    val totalActiveSignals: Int,
    val lastUpdateTime: Long,
    val apiStatus: String // "Online (Binance Live)", "Fallback Mode"
)

/**
 * Entitas Watchlist di Database Lokal
 */
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val baseAsset: String,
    val addedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

/**
 * Entitas Riwayat Sinyal di Database Lokal
 */
@Entity(tableName = "signal_history")
data class SignalHistoryEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val setupType: String,
    val triggerPrice: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val score: Int,
    val riskRewardRatio: Double,
    val createdAt: Long,
    val status: String,
    val outcome: String,
    val notes: String = ""
)

/**
 * Hasil Simulasi Backtest
 */
data class BacktestResult(
    val strategyName: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRatePercent: Double,
    val totalReturnPercent: Double,
    val profitFactor: Double,
    val maxDrawdownPercent: Double,
    val expectancyPerTrade: Double,
    val avgWinPercent: Double,
    val avgLossPercent: Double,
    val returnBeforeFeesPercent: Double,
    val returnAfterFeesPercent: Double,
    val trades: List<BacktestTrade>
)

data class BacktestTrade(
    val tradeId: String,
    val symbol: String,
    val entryDate: String,
    val exitDate: String,
    val setup: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val returnPercent: Double,
    val feeDeductionPercent: Double,
    val isWin: Boolean,
    val exitReason: String // "Target 1", "Target 2", "Stop Loss", "Trailing Stop"
)
