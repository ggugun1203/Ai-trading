package com.example.engine

import com.example.data.model.BacktestResult
import com.example.data.model.BacktestTrade
import com.example.data.model.Candle
import com.example.data.model.FilterSettings
import com.example.data.model.RiskSettings
import kotlin.math.abs
import kotlin.math.max

object BacktestEngine {

    /**
     * Jalankan simulasi backtest berbasis candle historis tanpa look-ahead bias
     */
    fun runBacktest(
        symbol: String,
        historicalCandles: List<Candle>,
        filterSettings: FilterSettings,
        riskSettings: RiskSettings
    ): BacktestResult {
        val totalCandles = historicalCandles.size
        if (totalCandles < 70) {
            return emptyResult("Data candle historis tidak mencukupi (minimal 70 candle).")
        }

        val trades = mutableListOf<BacktestTrade>()
        var inPosition = false
        var entryPrice = 0.0
        var stopLoss = 0.0
        var target1 = 0.0
        var target2 = 0.0
        var entryDate = ""
        var tradeSetup = ""
        var tradeIndex = 1

        val feeRate = (riskSettings.feePercent + riskSettings.slippagePercent) / 100.0 * 2 // Round-trip fee (entry + exit)

        // Iterasi dari candle ke-50 (membutuhkan warmup period untuk EMA dan RSI)
        for (i in 50 until totalCandles - 1) {
            // Gunakan hanya candle hingga index i (TIDAK MELIHAT MASA DEPAN)
            val availableCandles = historicalCandles.subList(0, i + 1)
            val currentCandle = availableCandles.last()

            if (!inPosition) {
                // Hitung indikator pada candle tertutup
                val indicators = TechnicalIndicatorEngine.computeAllIndicators(availableCandles) ?: continue
                val score = ScoringEngine.calculateScore(
                    price = currentCandle.close,
                    indicators = indicators,
                    volume24hUsd = currentCandle.volume * currentCandle.close,
                    spreadPercent = 0.05
                )

                // Cek sinyal
                val signal = SignalEngine.evaluateSignal(
                    symbol = symbol,
                    dailyCandles = availableCandles,
                    indicators = indicators,
                    scoreBreakdown = score,
                    filterSettings = filterSettings
                )

                if (signal != null && signal.status == com.example.data.model.SignalStatus.BUY_CANDIDATE) {
                    // Masuk posisi pada pembukaan candle berikutnya (i + 1) untuk mensimulasikan order real
                    val nextCandle = historicalCandles[i + 1]
                    entryPrice = nextCandle.open
                    stopLoss = signal.stopLoss
                    target1 = signal.target1
                    target2 = signal.target2
                    entryDate = "Day ${i + 1}"
                    tradeSetup = signal.setupType.title
                    inPosition = true
                }
            } else {
                // Posisi aktif: Evaluasi high & low candle i
                val high = currentCandle.high
                val low = currentCandle.low

                var closed = false
                var exitPrice = 0.0
                var exitReason = ""

                // Cek stop loss tertembus lebih dulu
                if (low <= stopLoss) {
                    exitPrice = stopLoss
                    exitReason = "Stop Loss"
                    closed = true
                } else if (high >= target2) {
                    exitPrice = target2
                    exitReason = "Target 2 (Runner)"
                    closed = true
                } else if (high >= target1) {
                    exitPrice = target1
                    exitReason = "Target 1 (1:2 R:R)"
                    closed = true
                }

                if (closed) {
                    val grossReturn = ((exitPrice - entryPrice) / entryPrice) * 100.0
                    val netReturn = grossReturn - (feeRate * 100.0)
                    val isWin = netReturn > 0

                    trades.add(
                        BacktestTrade(
                            tradeId = "TR_${tradeIndex++}",
                            symbol = symbol,
                            entryDate = entryDate,
                            exitDate = "Day ${i + 1}",
                            setup = tradeSetup,
                            entryPrice = entryPrice,
                            exitPrice = exitPrice,
                            returnPercent = netReturn,
                            feeDeductionPercent = feeRate * 100.0,
                            isWin = isWin,
                            exitReason = exitReason
                        )
                    )
                    inPosition = false
                }
            }
        }

        // Hitung statistik performa
        if (trades.isEmpty()) {
            return emptyResult("Tidak ada setup trading yang terpicu selama periode pengujian.")
        }

        val winningTrades = trades.filter { it.isWin }
        val losingTrades = trades.filter { !it.isWin }
        val winRate = (winningTrades.size.toDouble() / trades.size) * 100.0

        val totalGrossReturn = trades.sumOf { it.returnPercent + it.feeDeductionPercent }
        val totalNetReturn = trades.sumOf { it.returnPercent }

        val grossProfit = winningTrades.sumOf { it.returnPercent }
        val grossLoss = abs(losingTrades.sumOf { it.returnPercent })
        val profitFactor = if (grossLoss > 0) grossProfit / grossLoss else if (grossProfit > 0) 9.99 else 0.0

        val avgWin = if (winningTrades.isNotEmpty()) winningTrades.map { it.returnPercent }.average() else 0.0
        val avgLoss = if (losingTrades.isNotEmpty()) losingTrades.map { it.returnPercent }.average() else 0.0

        // Expectancy = (WinRate * AvgWin) - (LossRate * abs(AvgLoss))
        val lossRate = 1.0 - (winningTrades.size.toDouble() / trades.size)
        val expectancy = ((winRate / 100.0) * avgWin) - (lossRate * abs(avgLoss))

        // Maximum Drawdown calculation
        var peak = 0.0
        var currentEquity = 0.0
        var maxDrawdown = 0.0
        for (trade in trades) {
            currentEquity += trade.returnPercent
            if (currentEquity > peak) peak = currentEquity
            val dd = peak - currentEquity
            if (dd > maxDrawdown) maxDrawdown = dd
        }

        return BacktestResult(
            strategyName = "Crypto Scout AI Swing (1D/4H EMA+RVol)",
            totalTrades = trades.size,
            winningTrades = winningTrades.size,
            losingTrades = losingTrades.size,
            winRatePercent = winRate,
            totalReturnPercent = totalNetReturn,
            profitFactor = profitFactor,
            maxDrawdownPercent = maxDrawdown,
            expectancyPerTrade = expectancy,
            avgWinPercent = avgWin,
            avgLossPercent = avgLoss,
            returnBeforeFeesPercent = totalGrossReturn,
            returnAfterFeesPercent = totalNetReturn,
            trades = trades
        )
    }

    private fun emptyResult(message: String): BacktestResult {
        return BacktestResult(
            strategyName = "Crypto Scout AI Swing",
            totalTrades = 0,
            winningTrades = 0,
            losingTrades = 0,
            winRatePercent = 0.0,
            totalReturnPercent = 0.0,
            profitFactor = 0.0,
            maxDrawdownPercent = 0.0,
            expectancyPerTrade = 0.0,
            avgWinPercent = 0.0,
            avgLossPercent = 0.0,
            returnBeforeFeesPercent = 0.0,
            returnAfterFeesPercent = 0.0,
            trades = emptyList()
        )
    }
}
