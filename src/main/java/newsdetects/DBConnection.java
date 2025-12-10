package newsdetects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import jakarta.servlet.ServletContext;

public class DBConnection {
    private static Connection connection = null;
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    public static void init(ServletContext context) {
        try {
            Properties props = new Properties();
            props.load(context.getResourceAsStream("/WEB-INF/config.properties"));
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Database Driver not found!", e);
            }
        }
        return connection;
    }
}