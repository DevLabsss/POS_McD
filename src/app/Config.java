//package app;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class Config {
//    private static Connection conn;
//
//    public static Connection getConnection() {
//        if (conn == null) {
//            try {
//                String url = "jdbc:mysql://localhost:3306/pos_mcd";
//                String user = "root";
//                String pass = "";
//
//                conn = DriverManager.getConnection(url, user, pass);
//            } catch (SQLException e) {
//                System.out.println("Koneksi gagal: " + e.getMessage());
//            }
//        }
//        return conn;
//    }
//}
