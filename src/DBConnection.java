import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1/hotel_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL tidak ditemukan!");
        }
    }
}
