package com.promoit.otp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:otp_service;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            
            dataSource = new HikariDataSource(config);
            
            try (Connection conn = dataSource.getConnection()) {
                Statement stmt = conn.createStatement();
                
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY, " +
                    "login VARCHAR(100) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "salt VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "email VARCHAR(255), " +
                    "phone VARCHAR(20), " +
                    "telegram_chat_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                
                stmt.execute("CREATE SEQUENCE IF NOT EXISTS users_seq");
                
                stmt.execute("CREATE TABLE IF NOT EXISTS otp_config (" +
                    "id INT PRIMARY KEY, " +
                    "ttl_seconds INT NOT NULL DEFAULT 300, " +
                    "code_length INT NOT NULL DEFAULT 6)");
                
                stmt.execute("CREATE SEQUENCE IF NOT EXISTS otp_config_seq");
                
                stmt.execute("CREATE TABLE IF NOT EXISTS otp_codes (" +
                    "id INT PRIMARY KEY, " +
                    "operation_id VARCHAR(255) NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "code VARCHAR(10) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "expires_at TIMESTAMP NOT NULL, " +
                    "validated_at TIMESTAMP)");
                
                stmt.execute("CREATE SEQUENCE IF NOT EXISTS otp_codes_seq");
                
                stmt.execute("INSERT INTO otp_config (id, ttl_seconds, code_length) SELECT 1, 300, 6 WHERE NOT EXISTS (SELECT 1 FROM otp_config)");
                stmt.close();
                logger.info("Database tables created successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public static int nextId(String sequenceName) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT NEXT VALUE FOR " + sequenceName)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }
    
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
