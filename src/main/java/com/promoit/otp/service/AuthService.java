package com.promoit.otp.service;

import com.promoit.otp.dao.UserDao;
import com.promoit.otp.model.Role;
import com.promoit.otp.model.User;
import com.promoit.otp.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(String login, String password, String email, String phone, Role requestedRole) throws SQLException {
        if (requestedRole == Role.ADMIN && userDao.existsAdmin()) {
            throw new IllegalStateException("Admin already exists. Cannot register another admin.");
        }

        if (userDao.findByLogin(login) != null) {
            throw new IllegalStateException("User with this login already exists");
        }

        byte[] salt = PasswordUtil.generateSalt();
        String saltString = PasswordUtil.encodeSalt(salt);
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        User user = new User(login, passwordHash, requestedRole, email, phone);
        userDao.create(user, saltString);

        logger.info("New user registered: {} with role {}", login, requestedRole);
        return user;
    }

    public boolean verifyPassword(String password, String storedHash, byte[] salt) {
        return PasswordUtil.verifyPassword(password, storedHash, salt);
    }
}