# 🎓 StudentHub Mobile

**StudentHub** adalah aplikasi manajemen produktivitas dan akademik komprehensif yang dirancang khusus untuk mahasiswa. Dibangun sepenuhnya dengan **Kotlin** dan **Jetpack Compose**, aplikasi ini membantu pengguna untuk mengatur tugas akademik, jadwal kuliah, kegiatan organisasi (UKM), hingga melacak pencapaian dan target masa depan secara terstruktur.

## ✨ Fitur Utama

* 🏠 **Dashboard Terpusat**: Tampilan *Enhanced Dashboard* untuk melihat ringkasan aktivitas dan prioritas harian secara cepat.
* ✅ **Manajemen Tugas (Task Tracker)**: Kelola tugas berdasarkan kategori, tenggat waktu (*deadline*), dan tingkat prioritas (Tinggi, Sedang, Rendah). Dilengkapi dengan status penyelesaian (*checklist*) dan catatan tambahan.
* 📚 **Manajemen Kuliah (Subjects)**: Pantau mata kuliah yang sedang diambil dengan mudah.
* 👥 **Aktivitas UKM (Clubs)**: Atur kegiatan dan jadwal dari organisasi kemahasiswaan atau Unit Kegiatan Mahasiswa (UKM) yang Anda ikuti.
* 🎯 **Goals & Milestones Tracker**: Tetapkan target (*Goals*) dan pantau setiap langkah pencapaiannya (*Milestones*) secara terstruktur (Premium UI).
* 🏆 **Pencapaian (Achievements)**: Rekam jejak prestasi, sertifikat, dan *award* yang didapatkan selama masa studi.
* 📖 **Learning Resources**: Simpan tautan, referensi belajar, dan catatan penting dalam satu tempat agar mudah ditemukan saat dibutuhkan.
* 🎨 **UI/UX Modern & Premium**: Antarmuka dirancang dengan **Material Design 3**, mendukung penuh *Dark Mode* dan *Dynamic Colors*. Terdapat sentuhan animasi interaktif, efek bayangan, dan elemen gradien warna yang memanjakan mata.

## 🛠️ Teknologi yang Digunakan (Tech Stack)

Aplikasi ini mengadopsi standar arsitektur pengembangan Android modern (MVVM):

* **Bahasa Pemrograman**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Arsitektur**: MVVM (Model-View-ViewModel) menggunakan `ViewModel` dan `StateFlow`.
* **Database Lokal**: [Room Database](https://developer.android.com/training/data-storage/room) dengan KSP (*Kotlin Symbol Processing*).
* **Navigasi**: Compose Navigation (`NavHost`).
* **Desain Komponen**: Material 3 (M3).
* **Minimum SDK**: 24 (Android 7.0 Nougat)
* **Target SDK**: 36

## 🗂️ Entitas Database Utama

Aplikasi ini menggunakan relasi *Room Database* lokal yang mencakup entitas berikut:
* `Task`: Menyimpan data tugas (Judul, Tenggat Waktu, Prioritas, Kategori, Deskripsi).
* `CategoryTask`: Menyimpan label/kategori dari suatu tugas.
* `Subject`: Menyimpan profil mata kuliah.
* `Club`: Menyimpan profil dan kegiatan UKM.
* `Goal` & `GoalMilestone`: Menyimpan detail target jangka panjang/pendek beserta langkah-langkah *milestone*-nya.
* `Achievement`: Menyimpan rekapan prestasi.
* `LearningResource`: Menyimpan koleksi bahan belajar.

## 🚀 Cara Menjalankan Proyek

1. Pastikan Anda telah menginstal **Android Studio** versi terbaru.
2. Lakukan *Clone* / Unduh repositori ini ke dalam direktori lokal Anda.
3. Buka proyek ini melalui Android Studio.
4. Tunggu beberapa saat hingga proses sinkronisasi *Gradle* selesai sepenuhnya.
5. Klik tombol **Run** untuk menjalankan aplikasi pada *Android Emulator* atau perangkat fisik Anda.

---
*Dibuat untuk memudahkan produktivitas dan melacak seluruh perjalanan akademik perkuliahan Anda.*
