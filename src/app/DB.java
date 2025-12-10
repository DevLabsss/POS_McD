/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 *
 * @author mac
 */
public class DB {
    
    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {

                String url = "jdbc:mysql://localhost:3306/pos_mcd"; 
                String user = "root";
                String pass = ""; // kosong kalau XAMPP default

                conn = DriverManager.getConnection(url, user, pass);
                System.out.println("Koneksi OK");

            }
        } catch (Exception e) {
            System.out.println("Koneksi error: " + e.getMessage());
        }
        return conn;
    }

}

                
            