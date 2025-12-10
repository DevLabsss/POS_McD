package app;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import app.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *
 * @author mac
 */
public class NewJFrame extends javax.swing.JFrame {  
    
    public NewJFrame() {
        initComponents();
        Connection c = DB.getConnection();// TEST KONEKSI ke database pakai class DB
        
        // MENGATUR FONT AREA STRUK MONOSPACED
        // Supaya setiap karakter punya lebar sama, cocok buat layout struk
        b.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 10));

         // MENGATUR LEBAR KOLOM JTABLE - KOLOM ID & ITEM
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(30);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(200);
    }
        
    
    // FUNGSI INI MENAMBAHKAN / MENGUPDATE ITEM DI DALAM TABEL KERANJANG
    // DIPANGGIL SETIAP KALI USER KLIK SALAH SATU TOMBOL MENU
    public void addtable(int id ,String Name, int Qty , Double Price) {
        
        // ambil model dari JTable sebagai tempat menyimpan baris data
        DefaultTableModel dt = (DefaultTableModel) jTable1.getModel();
        
        // format angka jadi 2 angka di belakang koma, contoh: 2.99, 10.50
        DecimalFormat df = new DecimalFormat("0.00");

        
        //HITUNG TOTAL HARGA PERITEM (QTY*PRICE)
        double totPrice = Price * Double.valueOf(Qty);
        String TotalPrice = df.format(totPrice);

        
        // CEK APAKAH ITEM SUDAH ADA DI TABEL
        // JIKA SUDAH ADA, HAPUS DULU BARIS LAMA SUPAYA DIGANTI DENGAN QTY YANG BARU
        for (int row = 0; row < jTable1.getRowCount(); row++) {
            
            // cek nama item di kolom ke-1 (kolom item)
            if (Name.equals(jTable1.getValueAt(row, 1).toString())) {
                
                // hapus baris lama yang punya item sama
                dt.removeRow(jTable1.convertRowIndexToModel(row));
            }
        }
        
        
        
       // MENYIAPKAN DATA BARU DALAM BENTUK VECTOR (SATU BARIS TABEL)
        Vector v = new Vector();
        
        v.add(id);          // kolom 0 = id item
        v.add(Name);        // kolom 1 = nama item
        v.add(Qty);         // kolom 2 = qty
        v.add(TotalPrice);  // kolom 3 = total harga per item
        
        // TAMBAHKAN BARIS BARU KE DALAM TABEL
        dt.addRow(v);
    }
    
    
    
    //FUNGSI MERATAKAN TEKS CENTER UNTUK KEBUTUHAN STRUK
    private String centerText(String text) {
    int width = 42;                             // lebar maksimal satu baris karakter di struk
    int padSize = (width - text.length()) / 2;  // hitung berapa spasi di kiri

    if (padSize < 0) padSize = 0;               // kalau teksnya lebih panjang dari width, jangan minus
    
    // ulangi spasi sebanyak padSize, lalu gabung dengan teks
    return " ".repeat(padSize) + text;
}

    
    
    // FUNGSI MENGHITUNG TOTAL SEMUA ITEM DI TABEL
    public void cal() {
        
        int numOfRow = jTable1.getRowCount();   // jumlah baris di tabel (jumlah item)
        double tot = 0.0;                       // variabel untuk menyimpan total
        
        // LOOP SEMUA BARIS TABEL DAN JUMLAHKAN HARGA DI KOLOM KE-3 (price)
        for (int i = 0; i < numOfRow; i++) {
            double value = Double.valueOf(jTable1.getValueAt(i, 3).toString());
            tot += value ;
        }
        
        // format total jadi 2 angka di belakang koma
        DecimalFormat df = new DecimalFormat("0.00");
        // tampilkan total ke label "total"
        total.setText(df.format(tot));
    }
    
    
    
    // FUNGSI INI MENYIMPAN TRANSAKSI KE DATABASE:
    // 1. INSERT KE TABEL transactions
    // 2. INSERT DETAIL ITEM KE TABEL transaction_items
    private void simpanTransaksiKeDatabase(String noStruk) {
        
    // ambil model tabel untuk membaca data item
    DefaultTableModel df = (DefaultTableModel) jTable1.getModel();
    int rowCount = jTable1.getRowCount();
    
    // CEK JIKA TIDAK ADA ITEM DI KERANJANG
    if (rowCount == 0) {
        JOptionPane.showMessageDialog(this, "Tidak ada item di keranjang.");
        return; // hentikan fungsi
    }
    
    // CEK JIKA FIELD CASH MASIH KOSONG
    if (pay.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan nominal Cash terlebih dahulu.");
        return;
    }

    Connection conn = null;             // koneksi ke database
    PreparedStatement psTrans = null;   // statement untuk tabel transactions
    PreparedStatement psDetail = null;  // statement untuk tabel transaction_items
    ResultSet rsKey = null;             // untuk menampung generated key (id transaksi)

    try {
        conn = DB.getConnection();      // ambil koneksi dari class DB
        conn.setAutoCommit(false);      // mulai transaksi manual

        // ambil total belanja dari label total
        double totalBelanja = Double.parseDouble(total.getText());
        // ambil nominal uang cash dari textfield pay
        double cash = Double.parseDouble(pay.getText());

        // CEK UANG CUSTOMER CUKUP ATAU TIDAK
        if (cash < totalBelanja) {
            JOptionPane.showMessageDialog(this, "Uang customer kurang.");
            conn.setAutoCommit(true);   // balikin lagi ke auto-commit
            return;
        }

        // HITUNG KEMBALIAN DI SINI (SUPAYA LABEL BALANCE UP TO DATE)
        double balance = cash - totalBelanja;
        java.text.DecimalFormat dfmt = new java.text.DecimalFormat("0.00");
        bal.setText(dfmt.format(balance));  // update label kembalian

        String cashier = "KSR-01";          // contoh id kasir (hardcode)

        // QUERY INSERT UNTUK TABEL transactions
        String sqlTrans = "INSERT INTO transactions (date, total, cash, balance, cashier, no_struk) "
                        + "VALUES (NOW(), ?, ?, ?, ?, ?)";
        // siapkan statement dan minta generated key (id transaksi)
        psTrans = conn.prepareStatement(sqlTrans, PreparedStatement.RETURN_GENERATED_KEYS);
        psTrans.setDouble(1, totalBelanja); // total
        psTrans.setDouble(2, cash);         // cash
        psTrans.setDouble(3, balance);      // kembalian
        psTrans.setString(4, cashier);      // kasir
        psTrans.setString(5, noStruk);      // nomor struk
        psTrans.executeUpdate();            // eksekusi insert

        // ambil id transaksi yang baru saja dibuat (PRIMARY KEY)
        rsKey = psTrans.getGeneratedKeys();
        int transaksiId = 0;
        if (rsKey.next()) {
            transaksiId = rsKey.getInt(1);      // ambil nilai id pertama
        } else {
            throw new Exception("Gagal mengambil ID transaksi.");
        }

        // QUERY INSERT UNTUK TABEL transaction_items
        String sqlDetail = "INSERT INTO transaction_items (transaction_id, item_id, qty, price) "
                         + "VALUES (?, ?, ?, ?)";
        psDetail = conn.prepareStatement(sqlDetail);

        // LOOP SETIAP BARIS DI TABEL KERANJANG
        for (int i = 0; i < rowCount; i++) {
            int itemId = Integer.parseInt(df.getValueAt(i, 0).toString());     // kolom id di JTable = items.id
            int qty    = Integer.parseInt(df.getValueAt(i, 2).toString());     // kolom qty
            double price = Double.parseDouble(df.getValueAt(i, 3).toString()); // total per item
            
            // set parameter untuk INSERT detail
            psDetail.setInt(1, transaksiId);// foreign key ke tabel transactions
            psDetail.setInt(2, itemId);     // id item
            psDetail.setInt(3, qty);        // qty
            psDetail.setDouble(4, price);   // total harga per item
            psDetail.addBatch();            // tambahkan ke batch
        }
        
        // eksekusi semua INSERT detail sekaligus
        psDetail.executeBatch();
        // commit transaksi (simpan permanen ke database)
        conn.commit();

        JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan ke database.");

    } catch (Exception ex) {
        try {
            if (conn != null) conn.rollback();  // kalau error, batalkan semua perubahan
        } catch (Exception ignore) {}

        JOptionPane.showMessageDialog(this, "Gagal simpan transaksi: " + ex.getMessage());

    } finally {
        // TUTUP SEMUA RESOURCE SUPAYA TIDAK BOCOR
        try { if (rsKey != null) rsKey.close(); } catch (Exception e) {}
        try { if (psDetail != null) psDetail.close(); } catch (Exception e) {}
        try { if (psTrans != null) psTrans.close(); } catch (Exception e) {}
        try { 
            if (conn != null) { conn.setAutoCommit(true);   // kembalikan ke auto-commit
                conn.close();                                // tutup koneksi
            } 
        } catch (Exception e) {}
    }
}


    
    
    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        q1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        q2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        q8 = new javax.swing.JLabel();
        q4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        q3 = new javax.swing.JLabel();
        q6 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        q5 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        q9 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        q7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton12 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        b = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        total = new javax.swing.JLabel();
        bal = new javax.swing.JLabel();
        pay = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/1.jpg"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        q1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q1.setText("0");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/2.jpg"))); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        q2.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q2.setText("0");

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/8.jpg"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        q8.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q8.setText("0");

        q4.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q4.setText("0");

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/4.jpg"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/3.jpg"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        q3.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q3.setText("0");

        q6.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q6.setText("0");

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/6.jpg"))); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/5.jpg"))); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        q5.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q5.setText("0");

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/9.jpg"))); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        q9.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q9.setText("0");

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/app/images/10.jpg"))); // NOI18N
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        q7.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        q7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        q7.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(q7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(q8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(q9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(q4, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(q1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(q2, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(q5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(q3, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(q6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(q1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(q2, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addComponent(jButton5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(q3, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton7)
                            .addComponent(jButton4))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(q4, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(q5, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton9)
                                    .addComponent(jButton8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(q7, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(q8, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(q9, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jButton3)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(q6, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTable1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "item", "qty", "price"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton12.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jButton12.setText("Delete");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        b.setColumns(20);
        b.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        b.setRows(5);
        jScrollPane2.setViewportView(b);

        jLabel11.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel11.setText("Cash :");

        jLabel12.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel12.setText("Balance :");

        jLabel13.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel13.setText("Total :");

        total.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        total.setText("00");

        bal.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        bal.setText("00");

        pay.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N

        jButton10.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jButton10.setText("Pay");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jButton11.setText("Print");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13)
                    .addComponent(jLabel12))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(bal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(total, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(total))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel11)
                                    .addComponent(pay)))
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(bal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    //BUTTON MENU ITEM
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //btn code
        int i = Integer.valueOf(q1.getText());
        ++i;
        q1.setText(String.valueOf(i));
        
        addtable(1, "ZRW combo", i, 2.99 );
        
        cal();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //btn code
        int i = Integer.valueOf(q2.getText());
        ++i;
        q2.setText(String.valueOf(i));
        
        addtable(2, "1pc Combo", i, 2.59 );
        
        cal();
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //btn code
        int i = Integer.valueOf(q3.getText());
        ++i;
        q3.setText(String.valueOf(i));
        
        addtable(3, "2pc Combo", i, 7.99 );
        
        cal();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        //btn code
        int i = Integer.valueOf(q4.getText());
        ++i;
        q4.setText(String.valueOf(i));
        
        addtable(4, "Dinner Plate", i, 9.29);
        
        cal();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        //btn code
        int i = Integer.valueOf(q5.getText());
        ++i;
        q5.setText(String.valueOf(i));
        
        addtable(5, "Mini Bucket", i, 8.19);
        
        cal();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         //btn code
        int i = Integer.valueOf(q6.getText());
        ++i;
        q6.setText(String.valueOf(i));
        
        addtable(6, "Col Burger", i, 3.99);
        
        cal();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
         //btn code
        int i = Integer.valueOf(q7.getText());
        ++i;
        q7.setText(String.valueOf(i));
        
        addtable(7, "1pc Rice Plate", i, 12.99);
        
        cal();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        //btn code
        int i = Integer.valueOf(q8.getText());
        ++i;
        q8.setText(String.valueOf(i));
        
        addtable(8, "Lil' Combo", i, 13.99);
        
        cal();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        //btn code
        int i = Integer.valueOf(q9.getText());
        ++i;
        q9.setText(String.valueOf(i));
        
        addtable(9, "Rice Wrap", i, 12.99);
        
        cal();
    }//GEN-LAST:event_jButton8ActionPerformed

    
    
    
    // BUTTON DELETE - UNTUK MENGHAPUS SATU ITEM DARI TABEL KERANJANG
    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed

         DefaultTableModel dt = (DefaultTableModel) jTable1.getModel();
         
         // AMBIL INDEX BARIS YANG DIPILIH USER DI TABEL
         int rw = jTable1.getSelectedRow();
         
         // JIKA USER BELUM MEMILIH BARIS, LANGSUNG KELUAR DARI FUNGSI
         if (rw == -1) return;
         
         // AMBIL ID ITEM PADA BARIS TERSEBUT (KOL0M 0)
         String r = dt.getValueAt(rw, 0).toString();
         
         //HAPUS BARIS DARI TABEL
         dt.removeRow(rw);
         
         
         // RESET LABEL QTY YANG DITAMPILKAN DI ATAS TOMBOL SESUAI DENGAN ID ITEM
         switch(r) {
             case "1":
                 q1.setText("0");
                 break ;
                 
             case "2":
                 q2.setText("0");
                 break ;
                 
             case "3":
                 q3.setText("0");
                 break ;
                 
             case "4":
                 q4.setText("0");
                 break ;
                 
             case "5":
                 q5.setText("0");
                 break ;
                 
             case "6":
                 q6.setText("0");
                 break ;
                 
             case "7":
                 q7.setText("0");
                 break ;
                 
             case "8":
                 q8.setText("0");
                 break ;
                 
             case "9":
                 q9.setText("0");
                 break ;
         }
         
         // SETELAH ITEM DIHAPUS, HITUNG ULANG TOTAL
         cal();
    }//GEN-LAST:event_jButton12ActionPerformed

    
    
    
    // BUTTON PAY - MENGHITUNG KEMBALIAN DARI UANG YANG DIBAYAR CUSTOMER
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        
        // TOTAL BELANJA DIAMBIL DARI LABEL "total"
        double tot = Double.valueOf(total.getText());
        
        // UANG YANG DIBAYAR CUSTOMER DIAMBIL DARI TEXTFIELD "pay"
        double paid = Double.valueOf(pay.getText());
        
        // HITUNG KEMBALIAN = UANG BAYAR - TOTAL BELANJA
        double balance = paid - tot;
        
        // FORMAT KEMBALIAN 2 ANGKA DI BELAKANG KOMA
        DecimalFormat df = new DecimalFormat("0.00");
        
        // TAMPILKAN KEMBALIAN DI LABEL "bal"
        bal.setText(String.valueOf(df.format(balance)));
    }//GEN-LAST:event_jButton10ActionPerformed

    
    
    
    // BUTTON PRINT - MEMBUAT STRUK DI TEXTAREA DAN SEKALIGUS MENYIMPAN TRANSAKSI KE DATABASE
    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        //bil print
        
        try {
            // HEADER UTAMA STRUK (NAMA TOKO, ALAMAT, NPWP)
            b.setText(centerText("McDonald's Indonesia") + "\n");
            b.setText(b.getText() + centerText("Store #ID-4231 - Gading Serpong") + "\n");
            b.setText(b.getText() + "Jl. Raya Serpong KM.12, Tangerang\n");
            b.setText(b.getText() + "NPWP : 01.234.567.8-987.000\n");
            b.setText(b.getText() + "----------------------------------------------\n");

            // AMBIL TANGGAL & JAM SEKARANG
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String tanggal  = now.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String jam      = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            
            // TAMPILKAN TANGGAL, JAM, KASIR, DAN NOMOR STRUK
            b.setText(b.getText() + "Tanggal : " + tanggal + "   " + jam + "\n");
            b.setText(b.getText() + "Kasir   : KSR-01\n");
            
            // GENERATE NOMOR STRUK SECARA RANDOM (CONTOH SAJA)
            String noStruk = "TXN-" + String.format("%06d", (int)(Math.random() * 999999));
            b.setText(b.getText() + "No. Struk : " + noStruk + "\n");
            
            b.setText(b.getText() + "----------------------------------------------\n");
            b.setText(b.getText() + "Item                    Qty     Harga\n");
            b.setText(b.getText() + "----------------------------------------------\n");
             
             // AMBIL DATA ITEM DARI TABEL UNTUK DITAMPILKAN DI STRUK
             DefaultTableModel df = (DefaultTableModel) jTable1.getModel();
             
             for (int i = 0; i < jTable1.getRowCount(); i++) {

            String Name  = df.getValueAt(i, 1).toString().trim();   // nama item
            String Qty   = df.getValueAt(i, 2).toString();          // qty
            String Price = df.getValueAt(i, 3).toString();          // total harga per item

            // FORMAT TAMPILAN BARIS ITEM DI STRUK (RATA KIRI-KANAN)
            b.setText(b.getText() + String.format("%-20s %3s %10s\n",
                    Name,
                    Qty,
                    Price
            ));
        }
             
        b.setText(b.getText() + "----------------------------------------------\n");
        
        // TAMPILKAN SUBTOTAL, UANG BAYAR, DAN KEMBALIAN DI STRUK
        b.setText(b.getText() + String.format("Subtotal : %s\n", total.getText()));
        b.setText(b.getText() + String.format("Bayar    : %s\n", pay.getText()));
        b.setText(b.getText() + String.format("Kembali  : %s\n", bal.getText()));
        b.setText(b.getText() + "----------------------------------------------\n");

        // FOOTER STRUK - UCAPAN TERIMA KASIH
        b.setText(b.getText() + centerText("Terima kasih telah berkunjung!") + "\n");
        b.setText(b.getText() + centerText("Sampai jumpa di McDonald's") + "\n");
        b.setText(b.getText() + "----------------------------------------------\n");
        
        // SETELAH STRUK DIBUAT, SIMPAN JUGA DATA TRANSAKSI KE DATABASE
        simpanTransaksiKeDatabase(noStruk);
        
        } catch (Exception e) {
            // kalau ada error saat generate struk atau simpan ke DB, ditangkap di sini
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea b;
    private javax.swing.JLabel bal;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField pay;
    private javax.swing.JLabel q1;
    private javax.swing.JLabel q2;
    private javax.swing.JLabel q3;
    private javax.swing.JLabel q4;
    private javax.swing.JLabel q5;
    private javax.swing.JLabel q6;
    private javax.swing.JLabel q7;
    private javax.swing.JLabel q8;
    private javax.swing.JLabel q9;
    private javax.swing.JLabel total;
    // End of variables declaration//GEN-END:variables
}
