package com.promoit.otp;

import com.promoit.otp.config.DatabaseConfig;
import com.promoit.otp.controller.AuthController;
import com.promoit.otp.controller.UserController;
import com.promoit.otp.controller.AdminController;
import com.promoit.otp.dao.UserDao;
import com.promoit.otp.dao.OtpDao;
import com.promoit.otp.dao.OtpConfigDao;
import com.promoit.otp.service.*;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try {
            logger.info("Starting OTP Service...");
            
            UserDao userDao = new UserDao();
            OtpDao otpDao = new OtpDao();
            OtpConfigDao configDao = new OtpConfigDao();
            
            AuthService authService = new AuthService(userDao);
            EmailService emailService = new EmailService();
            SmsService smsService = new SmsService();
            TelegramService telegramService = new TelegramService("");
            FileService fileService = new FileService();
            OtpService otpService = new OtpService(otpDao, configDao, userDao, 
                    emailService, smsService, telegramService, fileService);
            
            AuthController authController = new AuthController(userDao, authService);
            UserController userController = new UserController(otpService, userDao);
            AdminController adminController = new AdminController(configDao, userDao, otpDao);
            
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/api/auth", authController);
            server.createContext("/api/user", userController);
            server.createContext("/api/admin", adminController);
            server.setExecutor(Executors.newFixedThreadPool(10));
            
            // Scheduler disabled temporarily
            // OtpExpiryScheduler scheduler = new OtpExpiryScheduler(otpDao);
            // scheduler.start();
            
            server.start();
            logger.info("OTP Service started successfully on port {}", PORT);
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }
}
