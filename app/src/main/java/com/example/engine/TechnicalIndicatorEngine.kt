package com.example.engine

import com.example.data.model.Candle
import com.example.data.model.IndicatorValues
import kotlin.math.abs
import kotlin.math.max

object TechnicalIndicatorEngine {

    /**
     * Hitung Exponential Moving Average (EMA)
     */
    fun calculateEMA(values: List<Double>, period: Int): List<Double> {
        if (values.size < period || period <= 0) return emptyList()

        val result = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1)

        // Seed dengan Simple Moving Average (SMA) awal
        val initialSma = values.take(period).average()
        result.add(initialSma)

        var currentEma = initialSma
        for (i in period until values.size) {
            currentEma = (values[i] - currentEma) * multiplier + currentEma
            result.add(currentEma)
        }
        return result
    }

    /**
     * Hitung Simple Moving Average (SMA)
     */
    fun calculateSMA(values: List<Double>, period: Int): List<Double> {
        if (values.size < period || period <= 0) return emptyList()
        val result = mutableListOf<Double>()
        for (i in period..values.size) {
            val subList = values.subList(i - period, i)
            result.add(subList.average())
        }
        return result
    }

    /**
     * Hitung Relative Strength Index (RSI) periode 14 dengan smoothing Wilder
     */
    fun calculateRSI(closes: List<Double>, period: Int = 14): List<Double> {
        if (closes.size <= period) return emptyList()

        val changes = mutableListOf<Double>()
        for (i in 1 until closes.size) {
            changes.add(closes[i] - closes[i - 1])
        }

        var gainSum = 0.0
        var lossSum = 0.0

        for (i in 0 until period) {
            val change = changes[i]
            if (change >= 0) gainSum += change else lossSum += abs(change)
        }

        var avgGain = gainSum / period
        var avgLoss = lossSum / period

        val rsiList = mutableListOf<Double>()
        val firstRsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + (avgGain / avgLoss)))
        rsiList.add(firstRsi)

        for (i in period until changes.size) {
            val change = changes[i]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period

            val rsi = if (avgLoss == 0.0) {
                100.0
            } else {
                val rs = avgGain / avgLoss
                100.0 - (100.0 / (1.0 + rs))
            }
            rsiList.add(rsi)
        }
        return rsiList
    }

    /**
     * Hitung MACD (12, 26, 9)
     * Mengembalikan Triple(macdLine, signalLine, histogram)
     */
    fun calculateMACD(
        closes: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): Triple<List<Double>, List<Double>, List<Double>> {
        if (closes.size < slowPeriod + signalPeriod) {
            return Triple(emptyList(), emptyList(), emptyList())
        }

        val fastEma = calculateEMA(closes, fastPeriod)
        val slowEma = calculateEMA(closes, slowPeriod)

        // Selaraskan offset karena slowEma dimulai lebih lambat
        val offset = slowPeriod - fastPeriod
        val macdLine = mutableListOf<Double>()
        for (i in slowEma.indices) {
            macdLine.add(fastEma[i + offset] - slowEma[i])
        }

        val signalLine = calculateEMA(macdLine, signalPeriod)
        val macdOffset = macdLine.size - signalLine.size
        val histogram = mutableListOf<Double>()

        for (i in signalLine.indices) {
            histogram.add(macdLine[i + macdOffset] - signalLine[i])
        }

        return Triple(macdLine, signalLine, histogram)
    }

    /**
     * Hitung Average True Range (ATR 14)
     */
    fun calculateATR(
        highs: List<Double>,
        lows: List<Double>,
        closes: List<Double>,
        period: Int = 14
    ): List<Double> {
        val n = closes.size
        if (n <= period || highs.size != n || lows.size != n) return emptyList()

        val trueRanges = mutableListOf<Double>()
        // Bar pertama
        trueRanges.add(highs[0] - lows[0])

        for (i in 1 until n) {
            val highLow = highs[i] - lows[i]
            val highClose = abs(highs[i] - closes[i - 1])
            val lowClose = abs(lows[i] - closes[i - 1])
            val tr = max(highLow, max(highClose, lowClose))
            trueRanges.add(tr)
        }

        // Wilder smoothing untuk ATR
        val atrList = mutableListOf<Double>()
        var currentAtr = trueRanges.take(period).average()
        atrList.add(currentAtr)

        for (i in period until trueRanges.size) {
            currentAtr = (currentAtr * (period - 1) + trueRanges[i]) / period
            atrList.add(currentAtr)
        }
        return atrList
    }

    /**
     * Hitung Support dan Resistance dari Pivot Swings historis
     */
    fun calculateSupportResistance(
        highs: List<Double>,
        lows: List<Double>,
        lookback: Int = 30
    ): Pair<Double, Double> {
        val windowHighs = highs.takeLast(lookback.coerceAtMost(highs.size))
        val windowLows = lows.takeLast(lookback.coerceAtMost(lows.size))

        val resistance = windowHighs.maxOrNull() ?: 0.0
        val support = windowLows.minOrNull() ?: 0.0
        return Pair(support, resistance)
    }

    /**
     * Hitung seluruh kumpulan indikator pada data candle tertutup
     */
    fun computeAllIndicators(closedCandles: List<Candle>): IndicatorValues? {
        // Butuh minimal 60-100 candle tertutup untuk EMA200 dan MACD/RSI yang akurat
        if (closedCandles.size < 50) return null

        val closes = closedCandles.map { it.close }
        val highs = closedCandles.map { it.high }
        val lows = closedCandles.map { it.low }
        val volumes = closedCandles.map { it.volume }

        val ema20List = calculateEMA(closes, 20)
        val ema50List = calculateEMA(closes, 50)
        val ema200List = if (closes.size >= 200) calculateEMA(closes, 200) else calculateEMA(closes, closes.size.coerceAtMost(100))

        val rsiList = calculateRSI(closes, 14)
        val (_, _, macdHist) = calculateMACD(closes, 12, 26, 9)
        val macdFull = calculateMACD(closes, 12, 26, 9)
        val atrList = calculateATR(highs, lows, closes, 14)

        val volumeSmaList = calculateSMA(volumes, 20)

        val lastClose = closes.last()
        val prevClose = if (closes.size > 1) closes[closes.size - 2] else lastClose
        val priceChange24h = ((lastClose - prevClose) / prevClose) * 100.0

        val (sup, res) = calculateSupportResistance(highs, lows, 30)

        val ema20 = ema20List.lastOrNull() ?: lastClose
        val ema50 = ema50List.lastOrNull() ?: lastClose
        val ema200 = ema200List.lastOrNull() ?: ema50
        val rsi14 = rsiList.lastOrNull() ?: 50.0
        val macdLine = macdFull.first.lastOrNull() ?: 0.0
        val macdSignal = macdFull.second.lastOrNull() ?: 0.0
        val macdHistogram = macdHist.lastOrNull() ?: 0.0
        val atr14 = atrList.lastOrNull() ?: (lastClose * 0.03)

        val volSma = volumeSmaList.lastOrNull() ?: volumes.last()
        val rvol = if (volSma > 0) volumes.last() / volSma else 1.0

        val momentumPercent = if (closes.size >= 10) {
            val close10Ago = closes[closes.size - 10]
            ((lastClose - close10Ago) / close10Ago) * 100.0
        } else priceChange24h

        return IndicatorValues(
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            rsi14 = rsi14,
            macdLine = macdLine,
            macdSignal = macdSignal,
            macdHist = macdHistogram,
            atr14 = atr14,
            volumeSma20 = volSma,
            relativeVolume = rvol,
            supportLevel = sup,
            resistanceLevel = res,
            priceChange24h = priceChange24h,
            momentumPercent = momentumPercent
        )
    }
}
