package com.litroenade.yunjiweather.auth;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class AuthPasswordUtils {

    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTE_LENGTH = 16;
    private static final int HASH_BIT_LENGTH = 256;
    private static final int ITERATION_COUNT = 120_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AuthPasswordUtils() {
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("密码长度必须为 6 到 32 个字符");
        }
    }

    public static String generateSalt() {
        byte[] bytes = new byte[SALT_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String hashPassword(String password, String salt) {
        validatePassword(password);
        if (salt == null || salt.trim().isEmpty()) {
            throw new IllegalArgumentException("密码盐不能为空");
        }
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, HASH_BIT_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = factory.generateSecret(keySpec).getEncoded();
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("密码盐格式不正确", exception);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("当前设备不支持安全密码哈希算法", exception);
        }
    }

    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        if (expectedHash == null || expectedHash.trim().isEmpty()) {
            return false;
        }
        String actualHash = hashPassword(password, salt);
        return constantTimeEquals(actualHash, expectedHash);
    }

    private static boolean constantTimeEquals(String first, String second) {
        byte[] firstBytes = first.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] secondBytes = second.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int result = firstBytes.length ^ secondBytes.length;
        int maxLength = Math.max(firstBytes.length, secondBytes.length);
        for (int i = 0; i < maxLength; i++) {
            byte firstByte = i < firstBytes.length ? firstBytes[i] : 0;
            byte secondByte = i < secondBytes.length ? secondBytes[i] : 0;
            result |= firstByte ^ secondByte;
        }
        return result == 0;
    }
}
