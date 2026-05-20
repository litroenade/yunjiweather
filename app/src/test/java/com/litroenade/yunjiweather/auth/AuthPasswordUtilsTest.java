package com.litroenade.yunjiweather.auth;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AuthPasswordUtilsTest {

    @Test
    public void hashPassword_samePasswordWithDifferentSalt_generatesDifferentHash() {
        String firstSalt = AuthPasswordUtils.generateSalt();
        String secondSalt = AuthPasswordUtils.generateSalt();

        String firstHash = AuthPasswordUtils.hashPassword("secret123", firstSalt);
        String secondHash = AuthPasswordUtils.hashPassword("secret123", secondSalt);

        assertNotEquals(firstSalt, secondSalt);
        assertNotEquals(firstHash, secondHash);
    }

    @Test
    public void verifyPassword_acceptsCorrectPassword() {
        String salt = AuthPasswordUtils.generateSalt();
        String hash = AuthPasswordUtils.hashPassword("secret123", salt);

        assertTrue(AuthPasswordUtils.verifyPassword("secret123", salt, hash));
    }

    @Test
    public void verifyPassword_rejectsWrongPassword() {
        String salt = AuthPasswordUtils.generateSalt();
        String hash = AuthPasswordUtils.hashPassword("secret123", salt);

        assertFalse(AuthPasswordUtils.verifyPassword("wrong123", salt, hash));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePassword_rejectsEmptyPassword() {
        AuthPasswordUtils.validatePassword("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePassword_rejectsShortPassword() {
        AuthPasswordUtils.validatePassword("12345");
    }
}
