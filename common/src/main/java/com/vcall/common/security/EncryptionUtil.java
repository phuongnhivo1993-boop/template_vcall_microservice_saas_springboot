package com.vcall.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey key;

    private static final int AES_KEY_SIZE = 32;

    public EncryptionUtil(@Value("${security.encryption.secret:${ENCRYPTION_SECRET:}}") String secret,
                          @Value("${spring.profiles.active:}") String activeProfile) {
        boolean isProduction = activeProfile != null &&
                (activeProfile.contains("prod") || activeProfile.contains("production"));

        if (secret == null || secret.isEmpty()) {
            if (isProduction) {
                log.error("ENCRYPTION_SECRET is not set. Refusing to start in production with an auto-generated key.");
                throw new IllegalStateException("ENCRYPTION_SECRET must be set in production environment");
            }
            byte[] fallback = new byte[AES_KEY_SIZE];
            new SecureRandom().nextBytes(fallback);
            this.key = new SecretKeySpec(fallback, "AES");
            log.warn("Using auto-generated encryption key. Set ENCRYPTION_SECRET environment variable in production.");
        } else {
            try {
                javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                java.security.spec.KeySpec spec = new javax.crypto.spec.PBEKeySpec(
                        secret.toCharArray(),
                        "VCallSalt".getBytes(StandardCharsets.UTF_8),
                        65536,
                        AES_KEY_SIZE * 8);
                javax.crypto.SecretKey tmp = factory.generateSecret(spec);
                this.key = new SecretKeySpec(tmp.getEncoded(), "AES");
            } catch (Exception e) {
                log.error("Failed to derive encryption key: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize encryption", e);
            }
        }
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(encryptedData));
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
