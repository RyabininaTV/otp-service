package com.promoit.otp.dao;

import com.promoit.otp.config.DatabaseConfig;
import com.promoit.otp.model.Role;
import com.promoit.otp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    
    public User create(User user, String salt) throws SQLException {
        int id = DatabaseConfig.nextId("users_seq");
        String sql = "INSERT INTO users (id, login, password_hash, salt, role, email, phone, telegram_chat_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, user.getLogin());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, salt);
            pstmt.setString(5, user.getRole().name());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getPhone());
            if (user.getTelegramChatId() != null) {
                pstmt.setLong(8, user.getTelegramChatId());
            } else {
                pstmt.setNull(8, Types.BIGINT);
            }
            pstmt.executeUpdate();
            user.setId((long) id);
            return user;
        }
    }
    
    public User findByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM users WHERE login = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }
    
    public String findSaltByLogin(String login) throws SQLException {
        String sql = "SELECT salt FROM users WHERE login = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("salt");
                }
            }
        }
        return null;
    }
    
    public boolean existsAdmin() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    public List<User> findAllNonAdmins() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'USER'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        }
        return users;
    }
    
    public boolean deleteUser(Long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ? AND role = 'USER'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setLogin(rs.getString("login"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        Long chatId = (Long) rs.getObject("telegram_chat_id");
        if (chatId != null) {
            user.setTelegramChatId(chatId);
        }
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}
