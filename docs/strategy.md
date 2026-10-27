# Strategi Swing Trading Crypto Scout AI

## 1. Filosofi Strategi
Crypto Scout AI dirancang khusus untuk strategi **Swing Trading** pada pasangan Spot kripto dengan timeframe utama **1D (Daily)** dan konfirmasi **4H**. Pendekatan ini menghindari kebisingan (noise) intraday dan berfokus pada momentum akumulasi institusional.

## 2. Rumus Skoring Peluang (0 - 100)
Skor dihitung secara matematis menggunakan bobot:
$$\text{Score} = 0.30 \times \text{Trend} + 0.20 \times \text{Momentum} + 0.20 \times \text{Volume} + 0.20 \times \text{Setup} + 0.10 \times \text{Risk}$$

- **Kekuatan Tren (30%)**: Penjajaran EMA 20 > EMA 50 > EMA 200, kemiringan EMA 50, dan toleransi jarak harga.
- **Momentum (20%)**: Zona RSI optimal (52-66), konfirmasi histogram MACD positif dan melebar, momentum 10-hari.
- **Volume (20%)**: Relative Volume (RVol) $\ge 1.2\times$, likuiditas spot 24h $\ge \$2\text{M}$, dominasi volume candle hijau.
- **Setup Teknikal (20%)**: Validasi pola Breakout atau Pullback ke zona support.
- **Kondisi Risiko (10%)**: Jarak Stop Loss yang proporsional (2-6%), volatilitas ATR yang terkendali, dan spread likuiditas tipis.

## 3. Logika Sinyal & State Machine
- **BUY_CANDIDATE**: Memenuhi filter likuiditas, skor $\ge 75$, Reward-to-Risk $\ge 2.0:1$, dan timeframe 4H tidak membatalkan setup.
- **WAITING_CONFIRMATION**: Skor antara 65-74, menunggu konfirmasi volume atau penutupan candle berikutnya.
- **WATCH**: Struktur tren naik sehat namun belum mencapai titik pemicu entri yang optimal.
- **INVALIDATED**: Terjadi breakdown support, RSI 4H crash di bawah 32, atau struktur tren rusak.
- **EXPIRED**: Waktu tunggu pemicu terlampaui (3-7 hari).

## 4. Manajemen Risiko & Ukuran Posisi
- Modal Acuan: Rp1.000.000 – Rp5.000.000 (Default Rp3.000.000).
- Batas Risiko per Trade: Maksimal 1.0% (Default 0.5%).
- Rumus Ukuran Posisi:
  $$\text{Risk Amount} = \text{Capital} \times \frac{\text{Risk \%}}{100}$$
  $$\text{Risk Per Unit} = |\text{Entry Price} - \text{Stop Loss}|$$
  $$\text{Position Quantity} = \frac{\text{Risk Amount}}{\text{Risk Per Unit}}$$
  $$\text{Position Value} = \text{Position Quantity} \times \text{Entry Price}$$
