package net.chamosmp.net.ChamoOrders.paper.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatabaseManager {

 /*   public void connectLite() {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:plugins/TestPlugin/database.db");
    }

  */
    public void connectMy() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydatabase"); // Address of your running MySQL database
        config.setUsername("username"); // Username
        config.setPassword("password"); // Password
        config.setMaximumPoolSize(10); // Pool size defaults to 10

        config.addDataSourceProperty("", ""); // MISC settings to add
        HikariDataSource dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            // Use a try-with-resources here to autoclose the connection.
            PreparedStatement sql = connection.prepareStatement("SQL");
            // Execute statement
        } catch (Exception e) {
            // Handle any exceptions that arise from getting / handing the exception.
        }
    }
}


