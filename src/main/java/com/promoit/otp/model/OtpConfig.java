package com.promoit.otp.model;

public class OtpConfig {
    private Long id;
    private Integer ttlSeconds;
    private Integer codeLength;

    public OtpConfig() {
        this.ttlSeconds = 300; // 5 minutes default
        this.codeLength = 6;   // 6 digits default
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(Integer ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    public Integer getCodeLength() { return codeLength; }
    public void setCodeLength(Integer codeLength) { this.codeLength = codeLength; }
}