package com.promoit.otp.service;

import com.promoit.otp.dao.OtpConfigDao;
import com.promoit.otp.dao.OtpDao;
import com.promoit.otp.dao.UserDao;
import com.promoit.otp.model.OtpCode;
import com.promoit.otp.model.OtpConfig;
import com.promoit.otp.model.User;
import com.promoit.otp.util.OtpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.UUID;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpDao otpDao;
    private final OtpConfigDao configDao;
    private final UserDao userDao;
    private final EmailService emailService;
    private final SmsService smsService;
    private final TelegramService telegramService;
    private final FileService fileService;

    public OtpService(OtpDao otpDao, OtpConfigDao configDao, UserDao userDao,
                      EmailService emailService, SmsService smsService,
                      TelegramService telegramService, FileService fileService) {
        this.otpDao = otpDao;
        this.configDao = configDao;
        this.userDao = userDao;
        this.emailService = emailService;
        this.smsService = smsService;
        this.telegramService = telegramService;
        this.fileService = fileService;
    }

    public String generateAndSendOtp(Long userId, String operationId, String preferredChannel) throws SQLException {
        User user = userDao.findByLogin(getLoginByUserId(userId));
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        OtpConfig config = configDao.getConfig();
        String code = OtpGenerator.generateCode(config.getCodeLength());

        if (operationId == null || operationId.isEmpty()) {
            operationId = UUID.randomUUID().toString();
        }

        OtpCode otpCode = new OtpCode(operationId, userId, code, config.getTtlSeconds());
        otpDao.create(otpCode);

        // Send via requested channel
        switch (preferredChannel.toLowerCase()) {
            case "email":
                if (user.getEmail() != null) {
                    emailService.sendCode(user.getEmail(), code, operationId);
                } else {
                    logger.warn("User {} has no email configured", userId);
                }
                break;
            case "sms":
                if (user.getPhone() != null) {
                    smsService.sendCode(user.getPhone(), code, operationId);
                } else {
                    logger.warn("User {} has no phone configured", userId);
                }
                break;
            case "telegram":
                if (user.getTelegramChatId() != null) {
                    telegramService.sendCode(user.getTelegramChatId(), code, operationId);
                } else {
                    logger.warn("User {} has no telegram chat ID configured", userId);
                }
                break;
            default:
                logger.warn("Unknown channel: {}, using file save only", preferredChannel);
        }

        // Always save to file
        fileService.saveCodeToFile(operationId, userId, code, preferredChannel);

        logger.info("OTP code generated for user {} operation {} via {}", userId, operationId, preferredChannel);
        return operationId;
    }

    public boolean validateOtp(Long userId, String operationId, String code) throws SQLException {
        boolean isValid = otpDao.validateCode(operationId, userId, code);

        fileService.saveValidationAttempt(operationId, userId, code, isValid);

        if (isValid) {
            logger.info("OTP code validated successfully for user {} operation {}", userId, operationId);
        } else {
            logger.warn("Invalid OTP code attempt for user {} operation {}: {}", userId, operationId, code);
        }

        return isValid;
    }

    private String getLoginByUserId(Long userId) throws SQLException {
        // This would need a method in UserDao to find by ID
        // For simplicity, we'll implement a helper method
        return findLoginByUserId(userId);
    }

    private String findLoginByUserId(Long userId) throws SQLException {
        String sql = "SELECT login FROM users WHERE id = ?";
        try (var conn = com.promoit.otp.config.DatabaseConfig.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("login");
                }
            }
        }
        return null;
    }
}