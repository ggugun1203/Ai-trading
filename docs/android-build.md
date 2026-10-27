# Panduan Build Android (Android Build Guide)

## 1. Persyaratan Lingkungan
- Java Development Kit (JDK): Versi 17 atau 21
- Android SDK: Platform API 34 atau 36
- Gradle: 8.x+
- Android Gradle Plugin: 9.x+

## 2. Perintah Build Lokal
Jalankan di root repositori:
```bash
# Menjalankan unit test
gradle testDebugUnitTest

# Membangun Debug APK
gradle assembleDebug

# Lokasi file output APK:
# app/build/outputs/apk/debug/app-debug.apk
```

## 3. Build Otomatis di GitHub Actions
Setiap push ke branch `main` akan memicu workflow `.github/workflows/android-build.yml`.
Setelah workflow selesai:
1. Buka tab **Actions** di repositori GitHub Anda.
2. Klik workflow run terbaru.
3. Unduh berkas dari bagian **Artifacts** dengan nama `crypto-scout-ai-debug-apk`.
