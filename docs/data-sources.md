# Sumber Data Pasar (Data Sources)

## 1. Integrasi API Binance Spot
Aplikasi mengambil data real-time langsung dari REST API publik Binance:
- **Ticker 24 Jam**: `GET https://api.binance.com/api/v3/ticker/24hr`
  Digunakan untuk menyaring pasangan USDT aktif, volume 24 jam, harga penutupan terakhir, dan statistik harian.
- **Candlestick Historis (Klines)**: `GET https://api.binance.com/api/v3/klines`
  Parameter: `symbol=<SYMBOL>`, `interval=1d`, `limit=120`.

## 2. Kebijakan Candle Tertutup (Closed Candle Policy)
Untuk mencegah sinyal palsu dan efek repaint:
- Hanya candle yang telah resmi ditutup (`isClosed = true` atau `System.currentTimeMillis() >= closeTime`) yang dihitung ke dalam indikator (EMA, RSI, MACD, ATR).
- Candle yang sedang berjalan (unfinished) tidak digunakan sebagai referensi penutupan.

## 3. Penanganan Rate Limit & Offline Fallback
- Menggunakan timeout 15 detik dengan OkHttp.
- Jika terjadi gangguan jaringan, rate limit exchange, atau mode offline, sistem beralih ke dataset cadangan berlabel jelas **"Mode Offline (Data Cadangan)"** tanpa membuat data fiktif seolah-olah data live.
