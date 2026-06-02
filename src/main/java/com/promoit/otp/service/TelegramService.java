package com.promoit.otp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TelegramService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private final String botToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramService(String botToken) {
        this.botToken = botToken;
        logger.info("Telegram service initialized");
    }

    public void sendCode(Long chatId, String code, String operationId) {
        if (chatId == null) {
            logger.warn("Cannot send Telegram message: chatId is null");
            return;
        }

        String message = String.format(
                "🔐 *OTP Code Requested*\n\n" +
                        "Operation ID: `%s`\n" +
                        "Your verification code: `%s`\n\n" +
                        "This code will expire in 5 minutes.\n" +
                        "If you didn't request this code, please secure your account.",
                operationId, code
        );

        String url = String.format("%s%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                TELEGRAM_API_URL,
                botToken,
                chatId,
                urlEncode(message));

        sendTelegramRequest(url);
    }

    public Long getChatIdFromUsername(String username) {
        String url = String.format("%s%s/getUpdates", TELEGRAM_API_URL, botToken);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode results = root.get("result");

                if (results != null && results.isArray()) {
                    for (JsonNode update : results) {
                        JsonNode message = update.get("message");
                        if (message != null) {
                            JsonNode from = message.get("from");
                            if (from != null) {
                                JsonNode usernameNode = from.get("username");
                                if (usernameNode != null && usernameNode.asText().equalsIgnoreCase(username)) {
                                    return from.get("id").asLong();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get chatId for username {}: {}", username, e.getMessage());
        }

        return null;
    }

    private void sendTelegramRequest(String url) {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Telegram API error. Status code: {}, Response: {}", response.statusCode(), response.body());
            } else {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                if (jsonResponse.has("ok") && jsonResponse.get("ok").asBoolean()) {
                    logger.info("Telegram message sent successfully");
                } else {
                    logger.error("Telegram API error: {}", jsonResponse);
                }
            }
        } catch (InterruptedException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage());
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}