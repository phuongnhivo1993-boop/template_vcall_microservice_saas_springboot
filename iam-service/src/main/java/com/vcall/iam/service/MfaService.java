package com.vcall.iam.service;

import com.vcall.iam.dto.MfaSetupResponse;
import com.vcall.iam.dto.MfaVerifyRequest;
import com.vcall.iam.entity.User;
import com.vcall.iam.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private static final String ISSUER = "VCall Contact Center";

    private final UserRepository userRepository;

    public MfaSetupResponse generateSetup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();

        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(QrData.Algorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        String qrCodeUri = qrData.getUri();
        MfaSetupResponse response = new MfaSetupResponse();
        response.setSecret(secret);
        response.setQrCodeUri(qrCodeUri);
        response.setEnabled(user.isMfaEnabled());
        return response;
    }

    @Transactional
    public boolean enableMfa(UUID userId, String secret, String code) {
        if (!verifyCode(secret, code)) {
            return false;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setMfaEnabled(true);
        user.setMfaSecret(secret);
        userRepository.save(user);
        log.info("MFA enabled for user {}", userId);
        return true;
    }

    @Transactional
    public void disableMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
        log.info("MFA disabled for user {}", userId);
    }

    public boolean verify(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isMfaEnabled() || user.getMfaSecret() == null) {
            return true;
        }
        return verifyCode(user.getMfaSecret(), code);
    }

    private boolean verifyCode(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secret, code);
    }
}
