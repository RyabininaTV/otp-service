package com.promoit.otp.dao;

import com.promoit.otp.config.DatabaseConfig;
import com.promoit.otp.model.OtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class OtpConfigDao {
    private static final Logger logger = LoggerFactory.getLogger(OtpConfigDao.class);
    
    public OtpConfig getConfig() throws SQLException {
        String sql = "SELECT id, ttl_seconds, code_length FROM otp_config LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                OtpConfig config = new OtpConfig();
                config.setId(rs.getLong("id"));
                config.setTtlSeconds(rs.getInt("ttl_seconds"));
                config.setCodeLength(rs.getInt("code_length"));
                return config;
            }
        }
        return new OtpConfig();
    }
    
    public void updateConfig(OtpConfig config) throws SQLException {
        String sql = "UPDATE otp_config SET ttl_seconds = ?, code_length = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, config.getTtlSeconds());
            pstmt.setInt(2, config.getCodeLength());
            pstmt.setLong(3, config.getId());
            pstmt.executeUpdate();
        }
    }
}
