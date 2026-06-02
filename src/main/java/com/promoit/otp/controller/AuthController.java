package com.promoit.otp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promoit.otp.dao.UserDao;
import com.promoit.otp.model.Role;
import com.promoit.otp.model.User;
import com.promoit.otp.service.AuthService;
import com.promoit.otp.util.JwtUtil;
import com.promoit.otp.util.PasswordUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthController implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserDao userDao;
    private final AuthService authService;
    
    public AuthController(UserDao userDao, AuthService authService) {
        this.userDao = userDao;
        this.authService = authService;
    }
    
    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            if ("POST".equals(method) && path.equals("/api/auth/register")) {
                handleRegister(exchange);
            } else if ("POST".equals(method) && path.equals("/api/auth/login")) {
                handleLogin(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            logger.error("Error handling request", e);
            try {
                sendResponse(exchange, 500, "{\"error\":\"Internal error: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                logger.error("Failed to send error response", ex);
            }
        }
    }
    
    private void handleRegister(HttpExchange exchange) throws Exception {
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining());
        
        logger.info("Register request body: {}", body);
        
        Map<String, String> req = objectMapper.readValue(body, Map.class);
        String login = req.get("login");
        String password = req.get("password");
        String email = req.get("email");
        String phone = req.getOrDefault("phone", "");
        String roleStr = req.getOrDefault("role", "USER");
        
        if (login == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Login and password required\"}");
            return;
        }
        
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            User user = authService.register(login, password, email, phone, role);
            sendResponse(exchange, 200, "{\"message\":\"User registered successfully\", \"id\":" + user.getId() + "}");
        } catch (IllegalStateException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Registration error", e);
            sendResponse(exchange, 500, "{\"error\":\"Registration failed: " + e.getMessage() + "\"}");
        }
    }
    
    private void handleLogin(HttpExchange exchange) throws Exception {
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining());
        
        logger.info("Login request body: {}", body);
        
        Map<String, String> req = objectMapper.readValue(body, Map.class);
        String login = req.get("login");
        String password = req.get("password");
        
        if (login == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Login and password required\"}");
            return;
        }
        
        User user = userDao.findByLogin(login);
        if (user == null) {
            sendResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            return;
        }
        
        String salt = userDao.findSaltByLogin(login);
        if (salt == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash(), PasswordUtil.decodeSalt(salt))) {
            sendResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            return;
        }
        
        String token = JwtUtil.generateToken(login, user.getRole().name());
        sendResponse(exchange, 200, "{\"token\":\"" + token + "\", \"role\":\"" + user.getRole().name() + "\"}");
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("Response {}: {}", statusCode, response);
    }
}
