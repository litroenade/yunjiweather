package com.litroenade.yunjiweather.auth;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class AuthPasswordUtils {

    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTE_LENGTH = 16;
    private static final int HASH_BIT_LENGTH = 256;
    private static final int ITERATION_COUNT = 120_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] BASE64_DECODE_TABLE = new int[128];

    static {
        Arrays.fill(BASE64_DECODE_TABLE, -1);
        for (int i = 0; i < BASE64_ALPHABET.length; i++) {
            BASE64_DECODE_TABLE[BASE64_ALPHABET[i]] = i;
        }
    }

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
        return encodeBase64(bytes);
    }

    public static String hashPassword(String password, String salt) {
        validatePassword(password);
        if (salt == null || salt.trim().isEmpty()) {
            throw new IllegalArgumentException("密码盐不能为空");
        }
        try {
            byte[] saltBytes = decodeBase64(salt);
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, HASH_BIT_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = factory.generateSecret(keySpec).getEncoded();
            return encodeBase64(hashBytes);
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

    private static String encodeBase64(byte[] bytes) {
        StringBuilder result = new StringBuilder(((bytes.length + 2) / 3) * 4);
        for (int i = 0; i < bytes.length; i += 3) {
            int first = bytes[i] & 0xFF;
            boolean hasSecond = i + 1 < bytes.length;
            boolean hasThird = i + 2 < bytes.length;
            int second = hasSecond ? bytes[i + 1] & 0xFF : 0;
            int third = hasThird ? bytes[i + 2] & 0xFF : 0;
            int combined = (first << 16) | (second << 8) | third;
            result.append(BASE64_ALPHABET[(combined >>> 18) & 0x3F]);
            result.append(BASE64_ALPHABET[(combined >>> 12) & 0x3F]);
            result.append(hasSecond ? BASE64_ALPHABET[(combined >>> 6) & 0x3F] : '=');
            result.append(hasThird ? BASE64_ALPHABET[combined & 0x3F] : '=');
        }
        return result.toString();
    }

    private static byte[] decodeBase64(String value) {
        String text = value.trim();
        if (text.length() % 4 != 0) {
            throw new IllegalArgumentException("Base64 长度不正确");
        }
        int padding = 0;
        if (text.endsWith("==")) {
            padding = 2;
        } else if (text.endsWith("=")) {
            padding = 1;
        }
        byte[] result = new byte[(text.length() / 4) * 3 - padding];
        int outputIndex = 0;
        for (int i = 0; i < text.length(); i += 4) {
            int first = decodeBase64Char(text.charAt(i));
            int second = decodeBase64Char(text.charAt(i + 1));
            int third = text.charAt(i + 2) == '=' ? 0 : decodeBase64Char(text.charAt(i + 2));
            int fourth = text.charAt(i + 3) == '=' ? 0 : decodeBase64Char(text.charAt(i + 3));
            int combined = (first << 18) | (second << 12) | (third << 6) | fourth;
            if (outputIndex < result.length) {
                result[outputIndex++] = (byte) ((combined >>> 16) & 0xFF);
            }
            if (outputIndex < result.length) {
                result[outputIndex++] = (byte) ((combined >>> 8) & 0xFF);
            }
            if (outputIndex < result.length) {
                result[outputIndex++] = (byte) (combined & 0xFF);
            }
        }
        return result;
    }

    private static int decodeBase64Char(char value) {
        if (value >= BASE64_DECODE_TABLE.length || BASE64_DECODE_TABLE[value] < 0) {
            throw new IllegalArgumentException("Base64 字符不合法");
        }
        return BASE64_DECODE_TABLE[value];
    }
}
