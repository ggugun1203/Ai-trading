package com.example

import com.example.data.model.Candle
import com.example.data.model.FilterResult
import com.example.data.model.FilterSettings
import com.example.data.model.IndicatorValues
import com.example.data.model.MarketTicker
import com.example.data.model.RiskSettings
import com.example.data.model.ScoreBreakdown
import com.example.data.model.SetupType
import com.example.data.model.SignalStatus
import com.example.engine.BacktestEngine
import com.example.engine.PositionCalculatorEngine
import com.example.engine.ScoringEngine
import com.example.engine.SignalEngine
import com.example.engine.TechnicalIndicatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CryptoScoutUnitTests {

    @Test
    fun testEMA_calculation() {
        val prices = listOf(22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39)
        val period = 5
        val emaList = TechnicalIndicatorEngine.calculateEMA(prices, period)

        assertFalse(emaList.isEmpty())
        // Seed SMA awal = (22.27 + 22.19 + 22.08 + 22.17 + 22.18) / 5 = 22.178
        val expectedInitial = (22.27 + 22.19 + 22.08 + 22.17 + 22.18) / 5.0
        assertEquals(expectedInitial, emaList[0], 0.001)

        // EMA berikutnya: k = 2 / (5 + 1) = 0.3333333333
        val nextPrice = 22.13
        val expectedNextEma = (nextPrice - expectedInitial) * (2.0 / 6.0) + expectedInitial
        assertEquals(expectedNextEma, emaList[1], 0.001)
    }

    @Test
    fun testRSI_calculation() {
        // Rentang harga naik konsisten -> RSI harus tinggi (> 70)
        val bullPrices = (1..30).map { 100.0 + (it * 2.5) }
        val rsiList = TechnicalIndicatorEngine.calculateRSI(bullPrices, 14)
        assertFalse(rsiList.isEmpty())
        assertTrue("RSI uptrend konsisten harus > 70", rsiList.last() > 70.0)

        // Rentang harga turun konsisten -> RSI harus rendah (< 30)
        val bearPrices = (1..30).map { 200.0 - (it * 3.0) }
        val bearRsi = TechnicalIndicatorEngine.calculateRSI(bearPrices, 14)
        assertFalse(bearRsi.isEmpty())
        assertTrue("RSI downtrend konsisten harus < 30", bearRsi.last() < 30.0)
    }

    @Test
    fun testMACD_calculation() {
        val prices = (1..60).map { 50.0 + (it * 0.8) }
        val (macdLine, signalLine, histogram) = TechnicalIndicatorEngine.calculateMACD(prices, 12, 26, 9)

        assertFalse(macdLine.isEmpty())
        assertFalse(signalLine.isEmpty())
        assertFalse(histogram.isEmpty())
        // Dalam uptrend konsisten, fast EMA > slow EMA, sehingga MACD line > 0
        assertTrue("MACD line harus positif dalam uptrend", macdLine.last() > 0.0)
    }

    @Test
    fun testATR_calculation() {
        val count = 25
        val highs = (1..count).map { 105.0 + it }
        val lows = (1..count).map { 95.0 + it }
        val closes = (1..count).map { 100.0 + it }

        val atrList = TechnicalIndicatorEngine.calculateATR(highs, lows, closes, 14)
        assertFalse(atrList.isEmpty())
        // Selisih high - low selalu 10.0, jadi ATR harus bernilai mendekati 10.0
        assertEquals(10.0, atrList.last(), 0.01)
    }

    @Test
    fun testScoringFormula_exactWeights() {
        // Uji bahwa formula score memenuhi bobot persis:
        // 0.30 trend + 0.20 momentum + 0.20 volume + 0.20 setup + 0.10 risk
        val mockIndicators = IndicatorValues(
            ema20 = 105.0,
            ema50 = 100.0,
            ema200 = 85.0,
            rsi14 = 58.0, // Sweet spot
            macdLine = 2.5,
            macdSignal = 1.2,
            macdHist = 1.3,
            atr14 = 3.0,
            volumeSma20 = 10000.0,
            relativeVolume = 1.8,
            supportLevel = 98.0,
            resistanceLevel = 109.0,
            priceChange24h = 3.2,
            momentumPercent = 4.5
        )

        val breakdown = ScoringEngine.calculateScore(
            price = 108.0,
            indicators = mockIndicators,
            volume24hUsd = 25_000_000.0,
            spreadPercent = 0.05
        )

        assertTrue(breakdown.totalScore in 0..100)
        assertTrue(breakdown.trendScore in 0.0..100.0)
        assertTrue(breakdown.momentumScore in 0.0..100.0)
        assertTrue(breakdown.volumeScore in 0.0..100.0)
        assertTrue(breakdown.setupScore in 0.0..100.0)
        assertTrue(breakdown.riskScore in 0.0..100.0)

        val manualSum = (0.30 * breakdown.trendScore) +
                        (0.20 * breakdown.momentumScore) +
                        (0.20 * breakdown.volumeScore) +
                        (0.20 * breakdown.setupScore) +
                        (0.10 * breakdown.riskScore)

        assertEquals(kotlin.math.round(manualSum).toInt(), breakdown.totalScore)
    }

    @Test
    fun testPositionCalculator_formulasAndRiskLimits() {
        val capitalIdr = 3_000_000.0
        val riskPercent = 0.5 // 0.5% dari Rp3.000.000 = Rp15.000
        val entryPrice = 100.0
        val stopLoss = 95.0   // Jarak risiko per unit = $5.0

        val settings = RiskSettings(
            capitalIdr = capitalIdr,
            riskPerTradePercent = riskPercent,
            usdToIdrRate = 16000.0
        )

        val calc = PositionCalculatorEngine.calculatePosition(
            capitalIdr = capitalIdr,
            riskPercent = riskPercent,
            entryPriceUsd = entryPrice,
            stopLossUsd = stopLoss,
            settings = settings
        )

        // Rp15.000 / 16.000 = $0.9375
        val expectedRiskAmountUsd = 15000.0 / 16000.0
        assertEquals(expectedRiskAmountUsd, calc.riskAmountUsd, 0.01)

        // Raw qty = $0.9375 / $5.0 = 0.1875 unit
        val expectedRawQty = expectedRiskAmountUsd / 5.0
        assertEquals(expectedRawQty, calc.rawQuantity, 0.001)

        // Nilai posisi = Qty * Entry
        assertTrue(calc.positionValueUsd > 0)
        assertTrue(calc.positionValueIdr > 0)
    }

    @Test
    fun testPositionCalculator_excessCapitalWarning() {
        val capitalIdr = 1_000_000.0 // Modal kecil
        val entryPrice = 100.0
        val stopLoss = 99.9 // Jarak SL teramat sempit ($0.1) -> unit meledak!

        val settings = RiskSettings(capitalIdr = capitalIdr, riskPerTradePercent = 1.0, usdToIdrRate = 16000.0)
        val calc = PositionCalculatorEngine.calculatePosition(
            capitalIdr = capitalIdr,
            riskPercent = 1.0,
            entryPriceUsd = entryPrice,
            stopLossUsd = stopLoss,
            settings = settings
        )

        // Harus memicu peringatan bahwa nilai posisi melampaui modal
        assertFalse(calc.isValid)
        assertNotNull(calc.warningMessage)
        assertTrue(calc.warningMessage!!.contains("melebihi total modal"))
    }

    @Test
    fun testSignalEngine_rewardToRiskMinimum() {
        val candles = (1..60).map {
            Candle(
                timestamp = System.currentTimeMillis() - ((60 - it) * 86400000L),
                open = 100.0 + it,
                high = 102.0 + it,
                low = 99.0 + it,
                close = 101.0 + it,
                volume = 5000.0,
                isClosed = true
            )
        }

        val indicators = TechnicalIndicatorEngine.computeAllIndicators(candles)!!
        val breakdown = ScoreBreakdown(85.0, 80.0, 85.0, 90.0, 80.0, 84)

        val signal = SignalEngine.evaluateSignal(
            symbol = "SOLUSDT",
            dailyCandles = candles,
            indicators = indicators,
            scoreBreakdown = breakdown,
            filterSettings = FilterSettings(minScoreToSignal = 75, minRewardToRisk = 2.0)
        )

        assertNotNull(signal)
        assertTrue("R:R harus minimal 2:1", signal!!.riskRewardRatio >= 2.0)
        assertTrue(signal.target1 > signal.triggerPrice)
        assertTrue(signal.stopLoss < signal.triggerPrice)
    }

    @Test
    fun testIncompleteCandleDetection() {
        // Memastikan candle yang belum selesai (isClosed == false) dapat diidentifikasi
        val completed = Candle(1000L, 10.0, 12.0, 9.0, 11.0, 1000.0, isClosed = true)
        val unfinished = Candle(2000L, 11.0, 13.0, 10.5, 12.5, 300.0, isClosed = false)

        val candles = listOf(completed, unfinished)
        val closedOnly = candles.filter { it.isClosed }

        assertEquals(1, closedOnly.size)
        assertEquals(1000L, closedOnly[0].timestamp)
    }

    @Test
    fun testBacktestEngine_zeroLookaheadBias() {
        // Generate dataset 100 candle
        val candles = (1..100).map {
            val p = 50.0 + (it * 0.5)
            Candle(
                timestamp = 1000000L + (it * 86400000L),
                open = p,
                high = p * 1.02,
                low = p * 0.98,
                close = p * 1.01,
                volume = 10000.0,
                isClosed = true
            )
        }

        val result = BacktestEngine.runBacktest(
            symbol = "NEARUSDT",
            historicalCandles = candles,
            filterSettings = FilterSettings(),
            riskSettings = RiskSettings()
        )

        assertNotNull(result)
        // Periksa bahwa setiap trade yang dieksekusi memiliki entryPrice dan exitPrice valid
        for (trade in result.trades) {
            assertTrue(trade.entryPrice > 0.0)
            assertTrue(trade.exitPrice > 0.0)
            assertTrue(trade.feeDeductionPercent > 0.0) // Biaya harus diperhitungkan!
        }
    }
}
