# Panduan Deployment & Keamanan

## 1. Arsitektur Tanpa Penyimpanan Kunci Privat (Non-Custodial)
- Crypto Scout AI adalah aplikasi analisis kuantitatif dan rekomendasi sinyal.
- Tidak meminta atau menyimpan private key wallet, seed phrase, atau API key trading dengan izin eksekusi order.
- Tidak melakukan transaksi otomatis (no automated trading).

## 2. Opsi Backend Python FastAPI (Opsional)
Kode backend referensi Python FastAPI disediakan di direktori `backend/` jika pengguna ingin mendeploy microservice server mandiri:
```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```
Layanan yang didukung untuk deployment backend: Google Cloud Run, Railway, Render, atau VPS Ubuntu dengan reverse proxy Nginx dan HTTPS.
