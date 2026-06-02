package com.promoit.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    public SmsService() {
        logger.info("SMS service initialized (simulator mode)");
    }

    public void sendCode(String phoneNumber, String code, String operationId) {
        // Simulate SMS sending
        logger.info("📱 SMS sent to {}: Your OTP code for operation {} is: {}", phoneNumber, operationId, code);
        System.out.println("=== SMS SIMULATOR ===");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: Your OTP code for operation " + operationId + " is: " + code);
        System.out.println("====================");
    }
}