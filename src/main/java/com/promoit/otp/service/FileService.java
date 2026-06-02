package com.promoit.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private static final String LOG_FILE = "otp_codes.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveCodeToFile(String operationId, Long userId, String code, String channel) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String logEntry = String.format("[%s] Operation: %s, User: %d, Code: %s, Channel: %s, Status: GENERATED",
                    LocalDateTime.now().format(FORMATTER),
                    operationId,
                    userId,
                    code,
                    channel);
            writer.println(logEntry);
            writer.flush();
            logger.info("OTP code saved to file for operation: {}", operationId);
        } catch (IOException e) {
            logger.error("Failed to save OTP code to file: {}", e.getMessage());
        }
    }

    public void saveValidationAttempt(String operationId, Long userId, String code, boolean success) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String logEntry = String.format("[%s] Operation: %s, User: %d, Code: %s, Validation: %s",
                    LocalDateTime.now().format(FORMATTER),
                    operationId,
                    userId,
                    code,
                    success ? "SUCCESS" : "FAILED");
            writer.println(logEntry);
            writer.flush();
        } catch (IOException e) {
            logger.error("Failed to save validation attempt to file: {}", e.getMessage());
        }
    }
}