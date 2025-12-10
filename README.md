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
POS_McD/
│
├── src/app/
│ ├── NewJFrame.java // UI utama + logika program
│ ├── DB.java // Koneksi database
│ ├── Config.java // Alternatif koneksi
│ └── images/ // Gambar menu
│
├── nbproject/ // Konfigurasi NetBeans
└── README.md

---

## 🛠 Teknologi yang Digunakan
- **Java Swing** (GUI)
- **MySQL / MariaDB**
- **JDBC**
- **NetBeans IDE**
- **DecimalFormat**, JTable, JTextArea, PreparedStatement

---

## ⚙ Cara Menjalankan Project

### 1. Import Database
Buat database:

```sql
CREATE DATABASE pos_mcd;

Import table :
USE pos_mcd;

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATETIME,
    total DOUBLE,
    cash DOUBLE,
    balance DOUBLE,
    cashier VARCHAR(50),
    no_struk VARCHAR(50)
);

CREATE TABLE transaction_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT,
    item_id INT,
    qty INT,
    price DOUBLE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);


###  2. Setting Koneksi (DB.java)
String url = "jdbc:mysql://localhost:3306/pos_mcd";
String user = "root";
String pass = "";

3. Run Project

Klik Run di NetBeans
→ Aplikasi POS siap digunakan.



