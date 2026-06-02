package com.promoit.otp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promoit.otp.dao.OtpConfigDao;
import com.promoit.otp.dao.OtpDao;
import com.promoit.otp.dao.UserDao;
import com.promoit.otp.model.OtpConfig;
import com.promoit.otp.model.User;
import com.promoit.otp.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OtpConfigDao configDao;
    private final UserDao userDao;
    private final OtpDao otpDao;
    
    public AdminController(OtpConfigDao configDao, UserDao userDao, OtpDao otpDao) {
        this.configDao = configDao;
        this.userDao = userDao;
        this.otpDao = otpDao;
    }
    
    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "{\"error\":\"Missing or invalid token\"}");
                return;
            }
            
            String token = authHeader.substring(7);
            String login = JwtUtil.getLoginFromToken(token);
            String role = JwtUtil.getRoleFromToken(token);
            
            if (login == null || !"ADMIN".equals(role)) {
                sendResponse(exchange, 403, "{\"error\":\"Access denied. Admin rights required\"}");
                return;
            }
            
            if ("GET".equals(method) && path.equals("/api/admin/config")) {
                handleGetConfig(exchange);
            } else if ("PUT".equals(method) && path.equals("/api/admin/config")) {
                handleUpdateConfig(exchange);
            } else if ("GET".equals(method) && path.equals("/api/admin/users")) {
                handleGetUsers(exchange);
            } else if ("DELETE".equals(method) && path.matches("/api/admin/users/\\d+")) {
                handleDeleteUser(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            logger.error("Error handling request", e);
            try {
                sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            } catch (Exception ex) {
                logger.error("Failed to send error response", ex);
            }
        }
    }
    
    private void handleGetConfig(HttpExchange exchange) throws Exception {
        OtpConfig config = configDao.getConfig();
        Map<String, Object> response = new HashMap<>();
        response.put("ttlSeconds", config.getTtlSeconds());
        response.put("codeLength", config.getCodeLength());
        sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
    }
    
    private void handleUpdateConfig(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        Map<String, Integer> request = objectMapper.readValue(body, Map.class);
        
        OtpConfig config = configDao.getConfig();
        if (request.containsKey("ttlSeconds")) {
            config.setTtlSeconds(request.get("ttlSeconds"));
        }
        if (request.containsKey("codeLength")) {
            config.setCodeLength(request.get("codeLength"));
        }
        
        configDao.updateConfig(config);
        sendResponse(exchange, 200, "{\"message\":\"Configuration updated successfully\"}");
    }
    
    private void handleGetUsers(HttpExchange exchange) throws Exception {
        List<User> users = userDao.findAllNonAdmins();
        List<Map<String, Object>> userList = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("login", u.getLogin());
            userMap.put("email", u.getEmail());
            userMap.put("phone", u.getPhone());
            userMap.put("createdAt", u.getCreatedAt().toString());
            userList.add(userMap);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
    }
    
    private void handleDeleteUser(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Long userId = Long.parseLong(parts[parts.length - 1]);
        
        boolean deleted = userDao.deleteUser(userId);
        if (deleted) {
            sendResponse(exchange, 200, "{\"message\":\"User deleted successfully\"}");
        } else {
            sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
        }
    }
    
    private String readRequestBody(HttpExchange exchange) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("Admin API Response: {} - {}", statusCode, response);
    }
}