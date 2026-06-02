package com.promoit.otp.model;

import java.time.LocalDateTime;

public class OtpCode {
    private Long id;
    private String operationId;
    private Long userId;
    private String code;
    private OtpStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;

    public OtpCode() {}

    public OtpCode(String operationId, Long userId, String code, Integer ttlSeconds) {
        this.operationId = operationId;
        this.userId = userId;
        this.code = code;
        this.status = OtpStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public OtpStatus getStatus() { return status; }
    public void setStatus(OtpStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }
}