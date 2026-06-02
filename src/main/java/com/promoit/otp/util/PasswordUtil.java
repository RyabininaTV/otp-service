package com.promoit.otp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    public static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static String encodeSalt(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    public static byte[] decodeSalt(String saltString) {
        return Base64.getDecoder().decode(saltString);
    }

    public static boolean verifyPassword(String password, String storedHash, byte[] salt) {
        String hash = hashPassword(password, salt);
        return hash.equals(storedHash);
    }
}