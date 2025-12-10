# 🍔 POS_McD  
Sistem Point of Sale (POS) sederhana berbasis **Java Swing + MySQL** yang dibuat untuk simulasi transaksi restoran cepat saji seperti McDonald's.  
Aplikasi ini mendukung fitur pemesanan menu, perhitungan total, pembayaran, cetak struk, dan penyimpanan transaksi ke database.

---

## 🚀 Fitur Utama

### ✔ 1. Manajemen Pesanan
- Klik tombol menu → item otomatis masuk ke tabel.
- Jumlah item bertambah setiap kali diklik.
- Harga total item dihitung otomatis.

### ✔ 2. Keranjang Belanja (JTable)
- Menampilkan daftar pesanan: ID, nama item, qty, dan total harga.
- Bisa menghapus item secara manual.

### ✔ 3. Perhitungan Otomatis
- Total belanja dihitung otomatis dari seluruh item.
- Input Cash → sistem menghitung Balance (kembalian).

### ✔ 4. Cetak Struk Otomatis
Struk mencakup:
- Nama restoran  
- Tanggal & jam  
- Nama kasir  
- Nomor struk unik  
- Daftar item (qty + harga)  
- Subtotal, bayar, kembalian  
- Pesan penutup  

Struk ditampilkan dalam JTextArea dengan format **monospaced**.

### ✔ 5. Penyimpanan Transaksi ke MySQL
Data tersimpan ke 2 tabel:

#### Table: `transactions`
- id  
- date  
- total  
- cash  
- balance  
- cashier  
- no_struk  

#### Table: `transaction_items`
- id  
- transaction_id  
- item_id  
- qty  
- price  

Menggunakan transaksi database (`setAutoCommit(false)`) untuk menjaga konsistensi data.

---

## 🗂 Struktur Project

