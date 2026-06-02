package com.promoit.otp.dao;

import com.promoit.otp.config.DatabaseConfig;
import com.promoit.otp.model.OtpCode;
import com.promoit.otp.model.OtpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class OtpDao {
    private static final Logger logger = LoggerFactory.getLogger(OtpDao.class);
    
    public OtpCode create(OtpCode otpCode) throws SQLException {
        int id = DatabaseConfig.nextId("otp_codes_seq");
        String sql = "INSERT INTO otp_codes (id, operation_id, user_id, code, status, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, otpCode.getOperationId());
            pstmt.setLong(3, otpCode.getUserId());
            pstmt.setString(4, otpCode.getCode());
            pstmt.setString(5, otpCode.getStatus().name());
            pstmt.setTimestamp(6, Timestamp.valueOf(otpCode.getExpiresAt()));
            pstmt.executeUpdate();
            otpCode.setId((long) id);
            return otpCode;
        }
    }
    
    public boolean validateCode(String operationId, Long userId, String code) throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'USED', validated_at = ? WHERE operation_id = ? AND user_id = ? AND code = ? AND status = 'ACTIVE' AND expires_at > NOW()";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, operationId);
            pstmt.setLong(3, userId);
            pstmt.setString(4, code);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public void expireOldCodes() throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at <= NOW()";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }
}
