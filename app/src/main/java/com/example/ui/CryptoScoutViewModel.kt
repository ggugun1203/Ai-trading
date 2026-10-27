package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BacktestResult
import com.example.data.model.Candle
import com.example.data.model.FilterSettings
import com.example.data.model.MarketOverview
import com.example.data.model.PositionCalculation
import com.example.data.model.RiskSettings
import com.example.data.model.SignalHistoryEntity
import com.example.data.model.SignalStatus
import com.example.data.model.WatchlistEntity
import com.example.data.repository.CryptoScoutRepository
import com.example.data.repository.ScannedAssetData
import com.example.engine.BacktestEngine
import com.example.engine.PositionCalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    DASHBOARD("Beranda"),
    SCANNER("Pemindai"),
    SIGNALS("Sinyal"),
    CALCULATOR("Kalkulator"),
    WATCHLIST("Watchlist"),
    SETTINGS("Pengaturan")
}

data class ScoutUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val marketOverview: MarketOverview? = null,
    val scannedAssets: List<ScannedAssetData> = emptyList(),
    val topCandidates: List<ScannedAssetData> = emptyList(),
    val selectedAsset: ScannedAssetData? = null,
    val selectedCandles: List<Candle> = emptyList(),
    val filterSettings: FilterSettings = FilterSettings(),
    val riskSettings: RiskSettings = RiskSettings(),
    val positionCalculation: PositionCalculation? = null,
    val backtestResult: BacktestResult? = null,
    val isBacktesting: Boolean = false,
    val errorMessage: String? = null,
    val activeTab: MainTab = MainTab.DASHBOARD,
    val filterOnlyPassed: Boolean = false,
    val searchQuery: String = "",
    val exportCsvContent: String? = null
)

class CryptoScoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CryptoScoutRepository(application)

    private val _uiState = MutableStateFlow(ScoutUiState())
    val uiState: StateFlow<ScoutUiState> = _uiState.asStateFlow()

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.getWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val signalHistory: StateFlow<List<SignalHistoryEntity>> = repository.getSignalHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        performInitialScan()
    }

    fun setTab(tab: MainTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFilterOnlyPassed() {
        _uiState.update { it.copy(filterOnlyPassed = !it.filterOnlyPassed) }
    }

    fun performInitialScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (overview, scanned) = repository.performMarketScan(
                    _uiState.value.filterSettings,
                    _uiState.value.riskSettings
                )
                val top5 = repository.rankTopCandidates(scanned)
                val firstAsset = top5.firstOrNull() ?: scanned.firstOrNull()
                val candles = firstAsset?.let { repository.fetchDailyCandles(it.ticker.symbol) } ?: emptyList()

                // Hitung kalkulasi posisi awal untuk aset pertama
                val posCalc = firstAsset?.signal?.let { sig ->
                    PositionCalculatorEngine.calculatePosition(
                        capitalIdr = _uiState.value.riskSettings.capitalIdr,
                        riskPercent = _uiState.value.riskSettings.riskPerTradePercent,
                        entryPriceUsd = sig.triggerPrice,
                        stopLossUsd = sig.stopLoss,
                        settings = _uiState.value.riskSettings
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        marketOverview = overview,
                        scannedAssets = scanned,
                        topCandidates = top5,
                        selectedAsset = firstAsset,
                        selectedCandles = candles,
                        positionCalculation = posCalc
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Gagal memindai pasar: ${e.localizedMessage ?: "Gangguan jaringan"}"
                    )
                }
            }
        }
    }

    fun refreshScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val (overview, scanned) = repository.performMarketScan(
                    _uiState.value.filterSettings,
                    _uiState.value.riskSettings
                )
                val top5 = repository.rankTopCandidates(scanned)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        marketOverview = overview,
                        scannedAssets = scanned,
                        topCandidates = top5
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = "Pembaruan gagal: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectAsset(symbol: String) {
        viewModelScope.launch {
            val asset = _uiState.value.scannedAssets.find { it.ticker.symbol == symbol }
                ?: return@launch

            val candles = repository.fetchDailyCandles(symbol)
            val posCalc = asset.signal?.let { sig ->
                PositionCalculatorEngine.calculatePosition(
                    capitalIdr = _uiState.value.riskSettings.capitalIdr,
                    riskPercent = _uiState.value.riskSettings.riskPerTradePercent,
                    entryPriceUsd = sig.triggerPrice,
                    stopLossUsd = sig.stopLoss,
                    settings = _uiState.value.riskSettings
                )
            } ?: run {
                // Fallback kalkulasi default stop loss 4%
                val entry = asset.ticker.lastPrice
                val sl = entry * 0.96
                PositionCalculatorEngine.calculatePosition(
                    capitalIdr = _uiState.value.riskSettings.capitalIdr,
                    riskPercent = _uiState.value.riskSettings.riskPerTradePercent,
                    entryPriceUsd = entry,
                    stopLossUsd = sl,
                    settings = _uiState.value.riskSettings
                )
            }

            _uiState.update {
                it.copy(
                    selectedAsset = asset,
                    selectedCandles = candles,
                    positionCalculation = posCalc
                )
            }
        }
    }

    fun updateRiskParameters(
        capitalIdr: Double,
        riskPercent: Double,
        usdRate: Double = _uiState.value.riskSettings.usdToIdrRate
    ) {
        val updatedSettings = _uiState.value.riskSettings.copy(
            capitalIdr = capitalIdr,
            riskPerTradePercent = riskPercent.coerceIn(0.1, 1.0),
            usdToIdrRate = usdRate
        )

        val currentAsset = _uiState.value.selectedAsset
        val entry = currentAsset?.signal?.triggerPrice ?: currentAsset?.ticker?.lastPrice ?: 100.0
        val sl = currentAsset?.signal?.stopLoss ?: (entry * 0.96)

        val newCalc = PositionCalculatorEngine.calculatePosition(
            capitalIdr = updatedSettings.capitalIdr,
            riskPercent = updatedSettings.riskPerTradePercent,
            entryPriceUsd = entry,
            stopLossUsd = sl,
            settings = updatedSettings
        )

        _uiState.update {
            it.copy(
                riskSettings = updatedSettings,
                positionCalculation = newCalc
            )
        }
    }

    fun updateCustomPosition(
        entryPrice: Double,
        stopLoss: Double
    ) {
        val newCalc = PositionCalculatorEngine.calculatePosition(
            capitalIdr = _uiState.value.riskSettings.capitalIdr,
            riskPercent = _uiState.value.riskSettings.riskPerTradePercent,
            entryPriceUsd = entryPrice,
            stopLossUsd = stopLoss,
            settings = _uiState.value.riskSettings
        )
        _uiState.update { it.copy(positionCalculation = newCalc) }
    }

    fun updateFilterSettings(newSettings: FilterSettings) {
        _uiState.update { it.copy(filterSettings = newSettings) }
        refreshScan()
    }

    fun toggleWatchlist(symbol: String, baseAsset: String) {
        viewModelScope.launch {
            repository.toggleWatchlist(symbol, baseAsset)
        }
    }

    fun runBacktestForSelected() {
        val asset = _uiState.value.selectedAsset ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBacktesting = true) }
            val candles = if (_uiState.value.selectedCandles.isNotEmpty()) {
                _uiState.value.selectedCandles
            } else {
                repository.fetchDailyCandles(asset.ticker.symbol, 180)
            }

            val result = BacktestEngine.runBacktest(
                symbol = asset.ticker.symbol,
                historicalCandles = candles,
                filterSettings = _uiState.value.filterSettings,
                riskSettings = _uiState.value.riskSettings
            )

            _uiState.update {
                it.copy(
                    isBacktesting = false,
                    backtestResult = result
                )
            }
        }
    }

    fun exportSignalHistoryCsv(): String {
        val history = signalHistory.value
        val sb = StringBuilder()
        sb.append("ID,Symbol,Setup,TriggerPrice,StopLoss,Target1,Target2,Score,RRR,CreatedAt,Status,Outcome\n")
        for (item in history) {
            sb.append("${item.id},${item.symbol},${item.setupType},${item.triggerPrice},${item.stopLoss},${item.target1},${item.target2},${item.score},${String.format("%.2f", item.riskRewardRatio)},${item.createdAt},${item.status},${item.outcome}\n")
        }
        val content = sb.toString()
        _uiState.update { it.copy(exportCsvContent = content) }
        return content
    }

    fun clearCsvExport() {
        _uiState.update { it.copy(exportCsvContent = null) }
    }
}
