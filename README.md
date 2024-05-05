# IF3210-2024-Android-CMS

## Deskripsi Aplikasi

BondoMan adalah aplikasi berbasis Android untuk pencatatan transaksi berupa pemasukan dan pengeluaran. Data transaksi yang disimpan antara lain adalah judul, kategori, nominal, lokasi, dan waktu transaksi. Pengguna dapat menambah, mengubah, dan menghapus transaksi. Pengguna juga dapat melakukan scan dan upload bill transaksi. Selain itu, tersedia grafik yang menggambarkan proporsi pemasukan dan pengeluaran yang tercatat. Pengguna juga dapat menyimpan daftar transaksi dalam bentuk file excel serta mengirimkan laporan transaksi melalui surel. Terdapat juga fitur untuk merandomisasi transaksi. Terakhir, terdapat fitur tambahan yaitu foto menggunakan twibbon.  

Pengguna dapat menggunakan fitur-fitur aplikasi setelah melakukan login. Otentikasi hanya berlaku lima menit sejak pengguna melakukan login, sehingga pengguna akan logout secara otomatis dan harus melakukan login kembali. 

## Library yang Digunakan

1. Kotlin standard library
2. AndroidX libraries for UI components, lifecycle management, navigation, work manager, fragment, room database, activity, and view model.
3. Compose libraries for UI development
4. Retrofit for API communication
5. OkHttp for HTTP client
6. Google Play Services for location and maps
7. Coroutines for asynchronous programming
8. Apache POI for handling Excel files
9. MPAndroidChart for plotting graphs
10. Core libraries for various Android functionalities such as animations, performance analysis, broadcast receivers, role management, shortcuts, remote views, and splash screen.

## Screenshot Aplikasi
<img src="./screenshot/splash_screen.jpg" alt="splash_screen"><br>
<img src="./screenshot/login_1.jpg" alt="login_1"><br>
<img src="./screenshot/login_2.jpg" alt="login_2"><br>
<img src="./screenshot/transactions.jpg" alt="transactions"><br>
<img src="./screenshot/new_transaction.jpg" alt="new_transaction"><br>
<img src="./screenshot/update_transaction.jpg" alt="update_transaction"><br>
<img src="./screenshot/scanner_1.jpg" alt="scanner_1"><br>
<img src="./screenshot/scanner_2.jpg" alt="scanner_2"><br>
<img src="./screenshot/scanner_gallery.jpg" alt="scanner_gallery"><br>
<img src="./screenshot/scanner_transaction.jpg" alt="scanner_transaction"><br>
<img src="./screenshot/graph_portrait.jpg" alt="graph_portrait"><br>
<img src="./screenshot/graph_landscape.jpg" alt="graph_landscape"><br>
<img src="./screenshot/settings.jpg" alt="settings"><br>
<img src="./screenshot/save_transactions.jpg" alt="save_transactions"><br>
<img src="./screenshot/send_email.jpg" alt="send_email"><br>
<img src="./screenshot/twibbon_1.jpg" alt="twibbon_1"><br>
<img src="./screenshot/twibbon_2.jpg" alt="twibbon_2"><br>
<img src="./screenshot/twibbon_3.jpg" alt="twibbon_3"><br>


## Pembagian Tugas
| Nama | NIM | Tugas |
|-----------------|-----------------|-----------------|
| Husnia Munzayana | 13521077 |CRUD transaksi, Lihat daftar transaksi, Simpan daftar transaksi, broadcast receiver, halaman tentang transaksi, intent gmail |
| Tabitha Permalla | 13521111 | ui/ux, Header Navbar, Login, Logout, Graf rangkuman, Background JWT Expiry, halaman splash, login, graph |
| Althaaf Khasyi Atisomya | 13521130 | Scan nota, Deteksi sinyal, init, db, halaman setting, scan, twibbon |


## Jumlah Jam Persiapan dan Pengerjaan

| Nama | NIM | Jumlah Jam Persiapan | Jumlah Jam Pengerjaan |
|-----------------|-----------------|-----------------|-----------------|
| Husnia Munzayana | 13521077 | 10 | 30 |
| Tabitha Permalla | 13521111 | 10 | 30 |
| Althaaf Khasyi Atisomya | 13521130 | 10 | 30 |
