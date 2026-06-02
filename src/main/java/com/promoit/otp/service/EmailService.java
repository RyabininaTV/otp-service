package com.promoit.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    public EmailService() {
        logger.info("Email service initialized (simulator mode)");
    }
    
    public void sendCode(String toEmail, String code, String operationId) {
        logger.info("📧 Email sent to {}: OTP for operation {} is: {}", toEmail, operationId, code);
        System.out.println("\n========== EMAIL SIMULATOR ==========");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: Your OTP Code for Operation: " + operationId);
        System.out.println("Body: Your verification code is: " + code);
        System.out.println("=====================================\n");
    }
}