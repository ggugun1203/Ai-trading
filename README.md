# Crypto Scout AI

Aplikasi analisis pasar aset kripto profesional untuk strategi **Swing Trading** pada pasangan Spot (Binance). Secara otomatis memindai aset, menyaring koin berdasarkan filter likuiditas & keamanan, memberikan skor peluang kuantitatif (0–100), menghitung ukuran posisi berbasis batasan risiko modal, dan menyediakan simulasi backtest tanpa look-ahead bias.

---

## Fitur Utama

1. **Dashboard Ringkasan Pasar**:
   - Harga langsung BTC & ETH beserta persentase 24 jam.
   - Status regim tren pasar keseluruhan.
   - Metrik jumlah aset dipindai, lolos filter, dan sinyal aktif.
   - Kartu 5 kandidat swing trading terbaik hari ini.
   - Indikator status koneksi API live (Binance Spot) vs mode cadangan.

2. **Crypto Scanner**:
   - Pemindaian pasangan spot USDT secara real-time.
   - Pencarian instan dan penyaringan fleksibel.
   - Penandaan faktor kegagalan filter jika aset tidak lolos.

3. **Filter Likuiditas & Keamanan**:
   - Usia candle historis minimal 180 hari.
   - Volume spot 24 jam minimal US$2 juta.
   - Median volume harian 30 hari minimal US$1 juta.
   - Deteksi anomali harga/volume dan audit status keamanan token ("Terverifikasi", "Audited").

4. **Mesin Analisis Teknikal & Skoring Kuantitatif (0–100)**:
   - Perhitungan pada candle tertutup: EMA 20, EMA 50, EMA 200, RSI 14, MACD (12, 26, 9), ATR 14, Volume SMA 20, Relative Volume (RVol), dan Pivot Support/Resistance.
   - Formula Pembobotan:
     $$\text{Skor} = 0.30 \times \text{Tren} + 0.20 \times \text{Momentum} + 0.20 \times \text{Volume} + 0.20 \times \text{Setup} + 0.10 \times \text{Risiko}$$

5. **Logika Sinyal Swing Trading**:
   - Dua setup teruji: **Breakout Terkonfirmasi** dan **Pullback pada Tren Naik**.
   - Status sinyal: `BUY_CANDIDATE`, `WAITING_CONFIRMATION`, `WATCH`, `INVALIDATED`, `EXPIRED`.
   - Level harga terukur: Harga Pemicu, Rentang Entri, Stop Loss indikatif, Target 1 (1:2 R:R), Target 2 (Runner), alasan sinyal, risiko utama, dan kondisi pembatalan.

6. **Kalkulator Manajemen Risiko Spot**:
   - Modal acuan dapat disesuaikan (Default Rp3.000.000).
   - Batas risiko per posisi (Default 0.5%, Maks 1.0%).
   - Rumus: $\text{Quantity} = \frac{\text{Risk Amount}}{\text{Risk Per Unit}}$.
   - Pembulatan step size exchange, konversi kurs USD/IDR, estimasi fee 0.1% + slippage, serta peringatan otomatis jika alokasi melampaui modal.

7. **Grafik Candlestick Interaktif**:
   - Custom Canvas candlestick chart dengan garis EMA 20, EMA 50, Support, Resistensi, dan histogram volume.

8. **Watchlist & Riwayat Sinyal**:
   - Penyimpanan lokal dengan Room Database.
   - Pencatatan seluruh sinyal untuk audit.
   - Fitur Ekspor ke CSV.

9. **Simulasi Backtest Historis**:
   - Pengujian strategi pada candle historis tanpa look-ahead bias.
   - Menampilkan Win Rate, Total Net Return, Profit Factor, Max Drawdown, Expectancy, dan perbandingan sebelum vs sesudah biaya.

---

## Panduan Menjalankan Aplikasi

### Android (Lokal)
```bash
# Menjalankan pengujian unit
gradle testDebugUnitTest

# Membangun file APK
gradle assembleDebug
```

### GitHub Actions (Otomatis)
Workflow build APK telah dikonfigurasi di `.github/workflows/android-build.yml`. Setiap push ke repositori akan otomatis memicu build dan menghasilkan artifact APK `crypto-scout-ai-debug-apk` yang dapat diunduh langsung dari tab **Actions**.

---

## Disclaimer
Aplikasi ini hanya berfungsi sebagai alat bantu analisis kuantitatif dan rekomendasi riset mandiri. Tidak melakukan transaksi otomatis, tidak meminta kunci privat dompet, dan bukan merupakan anjuran keuangan atau investasi.
