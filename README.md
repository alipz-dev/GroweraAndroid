# GroweraAndroid — Native Android starter

Proyek Android native untuk GROWERA Crystal Era menggunakan Kotlin + Jetpack Compose. Tidak menggunakan WebView atau HTML untuk UI.

## Yang sudah disiapkan
- Login native dan form validation.
- API client memakai Vercel rewrite yang sama dengan website: `https://growera-website.vercel.app/api/growera`.
- Penyimpanan token menggunakan AndroidX Security Crypto (`EncryptedSharedPreferences`).
- Dashboard native dasar dan navigasi Home / Profile / Rewards / Store / More.
- Tombol logout.
- GitHub Actions untuk membangun APK debug.

## Penting
1. Endpoint dan parsing respons belum diuji terhadap server langsung. Kode mencoba membaca variasi respons JSON, tetapi mungkin perlu penyesuaian berdasarkan respons API nyata.
2. Jika domain Vercel berbeda, ubah `API_BASE_URL` di `app/src/main/java/com/growera/app/data/ApiClient.kt`.
3. Jangan masukkan API secret atau kredensial admin ke APK.
4. APK debug bukan rilis produksi. Sebelum distribusi publik, audit autentikasi, token, privasi, dan signing.

## Build APK melalui GitHub Actions
1. Buat repositori baru bernama `GroweraAndroid` di GitHub.
2. Upload seluruh isi folder ini ke repositori tersebut (folder `.github` harus ikut ter-upload).
3. Buka tab **Actions**, jalankan workflow **Build Growera Android APK** (atau push ke branch `main`).
4. Setelah workflow selesai, unduh artifact `growera-debug-apk` dan ekstrak file APK.
5. Instal APK di Android 15. Android mungkin meminta izin instal aplikasi dari sumber tersebut.

## Build lokal
Diperlukan JDK 17, Android SDK (platform 35 dan build-tools), serta Gradle 8.9. Jalankan:

```bash
gradle assembleDebug
```

APK keluaran: `app/build/outputs/apk/debug/app-debug.apk`.
