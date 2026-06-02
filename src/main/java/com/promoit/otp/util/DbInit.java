package com.promoit.otp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbInit {
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:otp_service;DB_CLOSE_DELAY=-1", "sa", "")) {
            Statement stmt = conn.createStatement();
            
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "login VARCHAR(100) UNIQUE NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "salt VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "email VARCHAR(255), " +
                "phone VARCHAR(20), " +
                "telegram_chat_id BIGINT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS otp_config (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "ttl_seconds INT NOT NULL DEFAULT 300, " +
                "code_length INT NOT NULL DEFAULT 6)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS otp_codes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "operation_id VARCHAR(255) NOT NULL, " +
                "user_id INT NOT NULL, " +
                "code VARCHAR(10) NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "expires_at TIMESTAMP NOT NULL, " +
                "validated_at TIMESTAMP)");
            
            stmt.execute("INSERT INTO otp_config (ttl_seconds, code_length) SELECT 300, 6 WHERE NOT EXISTS (SELECT 1 FROM otp_config)");
            
            System.out.println("Tables created successfully!");
            stmt.close();
        }
    }
}
