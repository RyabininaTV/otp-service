package com.promoit.otp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promoit.otp.dao.UserDao;
import com.promoit.otp.service.OtpService;
import com.promoit.otp.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserController implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OtpService otpService;
    private final UserDao userDao;

    public UserController(OtpService otpService, UserDao userDao) {
        this.otpService = otpService;
        this.userDao = userDao;
    }

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Extract user from token
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "{\"error\":\"Missing or invalid token\"}");
                return;
            }

            String token = authHeader.substring(7);
            String login = JwtUtil.getLoginFromToken(token);
            String role = JwtUtil.getRoleFromToken(token);

            if (login == null || !"USER".equals(role)) {
                sendResponse(exchange, 403, "{\"error\":\"Access denied\"}");
                return;
            }

            if ("POST".equals(method) && path.equals("/api/user/generate-otp")) {
                handleGenerateOtp(exchange, login);
            } else if ("POST".equals(method) && path.equals("/api/user/validate-otp")) {
                handleValidateOtp(exchange, login);
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

    private void handleGenerateOtp(HttpExchange exchange, String login) throws Exception {
        String body = readRequestBody(exchange);
        Map<String, String> request = objectMapper.readValue(body, Map.class);

        String operationId = request.getOrDefault("operationId", java.util.UUID.randomUUID().toString());
        String channel = request.getOrDefault("channel", "email");

        var user = userDao.findByLogin(login);
        if (user == null) {
            sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
            return;
        }

        String generatedOpId = otpService.generateAndSendOtp(user.getId(), operationId, channel);

        sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of(
                "operationId", generatedOpId,
                "message", "OTP code generated and sent via " + channel
        )));
    }

    private void handleValidateOtp(HttpExchange exchange, String login) throws Exception {
        String body = readRequestBody(exchange);
        Map<String, String> request = objectMapper.readValue(body, Map.class);

        String operationId = request.get("operationId");
        String code = request.get("code");

        if (operationId == null || code == null) {
            sendResponse(exchange, 400, "{\"error\":\"operationId and code are required\"}");
            return;
        }

        var user = userDao.findByLogin(login);
        if (user == null) {
            sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
            return;
        }

        boolean isValid = otpService.validateOtp(user.getId(), operationId, code);

        sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of(
                "valid", isValid,
                "message", isValid ? "Code is valid" : "Code is invalid or expired"
        )));
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
        logger.info("Response: {} - {}", statusCode, response);
    }
}