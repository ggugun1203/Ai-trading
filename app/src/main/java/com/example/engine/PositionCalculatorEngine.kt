package com.example.engine

import com.example.data.model.PositionCalculation
import com.example.data.model.RiskSettings
import kotlin.math.abs
import kotlin.math.floor

object PositionCalculatorEngine {

    /**
     * Hitung ukuran posisi spot berbasis modal dan batasan risiko
     */
    fun calculatePosition(
        capitalIdr: Double,
        riskPercent: Double,
        entryPriceUsd: Double,
        stopLossUsd: Double,
        settings: RiskSettings
    ): PositionCalculation {
        // Validasi parameter
        val effectiveRiskPercent = riskPercent.coerceIn(0.1, 1.0) // Batas risiko maks 1.0%
        val riskAmountIdr = capitalIdr * (effectiveRiskPercent / 100.0)
        val usdRate = if (settings.usdToIdrRate > 0) settings.usdToIdrRate else 16000.0
        val riskAmountUsd = riskAmountIdr / usdRate

        val riskPerUnitUsd = abs(entryPriceUsd - stopLossUsd)

        if (entryPriceUsd <= 0.0 || stopLossUsd <= 0.0 || riskPerUnitUsd <= 0.0 || capitalIdr <= 0.0) {
            return PositionCalculation(
                capitalIdr = capitalIdr,
                riskPercent = effectiveRiskPercent,
                riskAmountIdr = riskAmountIdr,
                riskAmountUsd = riskAmountUsd,
                entryPriceUsd = entryPriceUsd,
                stopLossUsd = stopLossUsd,
                riskPerUnitUsd = riskPerUnitUsd,
                rawQuantity = 0.0,
                adjustedQuantity = 0.0,
                positionValueUsd = 0.0,
                positionValueIdr = 0.0,
                estimatedFeeUsd = 0.0,
                isValid = false,
                warningMessage = "Harga entri atau stop-loss tidak valid."
            )
        }

        // Rumus: position_quantity = risk_amount / risk_per_unit
        val rawQuantity = riskAmountUsd / riskPerUnitUsd
        val rawPositionValueUsd = rawQuantity * entryPriceUsd
        val rawPositionValueIdr = rawPositionValueUsd * usdRate

        // Aturan step size dan lot size: bulatkan ke presisi rasional
        val adjustedQuantity = roundToStepSize(rawQuantity, entryPriceUsd)
        val positionValueUsd = adjustedQuantity * entryPriceUsd
        val positionValueIdr = positionValueUsd * usdRate

        // Estimasi biaya transaksi (fee % maker/taker) + slippage
        val totalFeeRate = (settings.feePercent + settings.slippagePercent) / 100.0
        val estimatedFeeUsd = positionValueUsd * totalFeeRate

        // Evaluasi kelayakan posisi
        var isValid = true
        var warning: String? = null

        // 1. Apakah nilai posisi melebihi alokasi per posisi (misal maksimal 3 posisi terbuka => maks 33%-40% modal per posisi)
        val maxCapitalPerTradeIdr = capitalIdr / settings.maxPositions.coerceAtLeast(1)
        if (positionValueIdr > capitalIdr) {
            isValid = false
            warning = "PERINGATAN: Nilai posisi yang dihitung (Rp${formatIdr(positionValueIdr)}) melebihi total modal Anda (Rp${formatIdr(capitalIdr)}). Jarak stop loss terlalu sempit untuk modal ini!"
        } else if (positionValueIdr > maxCapitalPerTradeIdr * 1.1) {
            warning = "Perhatian: Nilai posisi (Rp${formatIdr(positionValueIdr)}) melampaui batas alokasi ideal 1 dari ${settings.maxPositions} posisi (Rp${formatIdr(maxCapitalPerTradeIdr)})."
        } else if (positionValueUsd < 5.0) {
            // Binance spot minimum order nominal umumnya $5 - $10
            isValid = false
            warning = "PERINGATAN: Nilai pesanan ($${String.format("%.2f", positionValueUsd)}) di bawah batas minimum order Binance Spot ($5.00). Tingkatkan modal atau sesuaikan stop loss."
        }

        return PositionCalculation(
            capitalIdr = capitalIdr,
            riskPercent = effectiveRiskPercent,
            riskAmountIdr = riskAmountIdr,
            riskAmountUsd = riskAmountUsd,
            entryPriceUsd = entryPriceUsd,
            stopLossUsd = stopLossUsd,
            riskPerUnitUsd = riskPerUnitUsd,
            rawQuantity = rawQuantity,
            adjustedQuantity = adjustedQuantity,
            positionValueUsd = positionValueUsd,
            positionValueIdr = positionValueIdr,
            estimatedFeeUsd = estimatedFeeUsd,
            isValid = isValid,
            warningMessage = warning
        )
    }

    private fun roundToStepSize(quantity: Double, price: Double): Double {
        return when {
            price >= 1000.0 -> floor(quantity * 10000.0) / 10000.0
            price >= 10.0 -> floor(quantity * 1000.0) / 1000.0
            price >= 1.0 -> floor(quantity * 100.0) / 100.0
            else -> floor(quantity * 10.0) / 10.0
        }
    }

    private fun formatIdr(amount: Double): String {
        return String.format("%,.0f", amount)
    }
}
