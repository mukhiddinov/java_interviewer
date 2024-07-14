package com.example.telegrambot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String url = "jdbc:postgresql://localhost:5432/interview_bot";
    private static final String user = "YOUR_POSTGRESQL_USER";
    private static final String password = "YOUR_PASSWORD";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
