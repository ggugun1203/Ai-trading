# Arsitektur Crypto Scout AI

## 1. Ikhtisar Sistem
Crypto Scout AI dibangun dengan arsitektur **Clean Architecture & MVVM (Model-View-ViewModel)** di atas Android Native (Kotlin & Jetpack Compose) dengan dukungan pipeline simulasi kuantitatif.

```
+-------------------------------------------------------------+
|                     Jetpack Compose UI                      |
| (Dashboard, Scanner, Signals, Calculator/Chart, Watchlist)  |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                    CryptoScoutViewModel                     |
|           (StateFlow, Coroutine Scope, UI State)            |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                   CryptoScoutRepository                     |
|       (Koordinasi Data Pasar, Cache, & Persistence)         |
+---------------+-----------------------------+---------------+
                |                             |
                v                             v
+-------------------------------+ +---------------------------+
|     Binance Spot API Client   | |      Room Database        |
|  (24h Ticker & 1D/4H Klines)  | |  (Watchlist & History)    |
+---------------+---------------+ +---------------------------+
                |
                v
+-------------------------------------------------------------+
|                  Mathematical Engines (Pure)                |
|  - TechnicalIndicatorEngine (EMA, RSI, MACD, ATR, RVol)     |
|  - ScoringEngine (30% Tren, 20% Mom, 20% Vol, 20% Setup...) |
|  - SignalEngine (Breakout & Pullback State Machine)         |
|  - PositionCalculatorEngine (Risk Sizing & Step Rules)      |
|  - BacktestEngine (Zero Look-Ahead Bias Simulator)          |
+-------------------------------------------------------------+
```

## 2. Lapisan Inti (Core Layers)
1. **Domain Models**: Merepresentasikan Candle, Ticker, Level Indikator, Breakdown Skor, Sinyal, dan Parameter Risiko.
2. **Mathematical Engine**: Berisi fungsi deterministik murni tanpa dependensi Android SDK agar 100% dapat diuji unit test.
3. **Data Layer**:
   - `BinanceApiService`: Mengambil candlestick aktual dari exchange Binance Spot.
   - `Room Database`: Menyimpan Watchlist dan audit log sinyal secara persisten di perangkat.
4. **Presentation Layer**: Dibangun menggunakan Material 3 Jetpack Compose dengan palet Dark Premium (Navy Deep Slate, Emerald Bull Green, Cyber Cyan, Alert Yellow).
