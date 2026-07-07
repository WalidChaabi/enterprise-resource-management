package com.company.erm.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.config.OAuth2Properties;
import com.company.erm.dto.MfaBackupCodesResponseDTO;
import com.company.erm.dto.MfaSetupResponseDTO;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.entity.UserMfaBackupCode;
import com.company.erm.entity.UserMfaSecret;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserMfaBackupCodeRepository;
import com.company.erm.repository.UserMfaSecretRepository;
import com.company.erm.repository.UserRepository;
import com.company.erm.security.SecretEncryptionService;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

@Service
@Transactional
public class MfaService {

    private static final int BACKUP_CODE_COUNT = 8;

    private final UserRepository userRepository;
    private final UserMfaSecretRepository mfaSecretRepository;
    private final UserMfaBackupCodeRepository backupCodeRepository;
    private final SecretEncryptionService secretEncryptionService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2Properties oauth2Properties;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final SecureRandom secureRandom = new SecureRandom();

    public MfaService(
            UserRepository userRepository,
            UserMfaSecretRepository mfaSecretRepository,
            UserMfaBackupCodeRepository backupCodeRepository,
            SecretEncryptionService secretEncryptionService,
            PasswordEncoder passwordEncoder,
            OAuth2Properties oauth2Properties) {
        this.userRepository = userRepository;
        this.mfaSecretRepository = mfaSecretRepository;
        this.backupCodeRepository = backupCodeRepository;
        this.secretEncryptionService = secretEncryptionService;
        this.passwordEncoder = passwordEncoder;
        this.oauth2Properties = oauth2Properties;
    }

    @Transactional(readOnly = true)
    public boolean isMfaRequired(User user) {
        if (mfaSecretRepository.existsById(user.getId())) {
            return true;
        }
        Organization org = user.getOrganization();
        return org != null && org.isRequireMfa();
    }

    @Transactional(readOnly = true)
    public boolean hasMfaEnrolled(UUID userId) {
        return mfaSecretRepository.existsById(userId);
    }

    @Transactional(readOnly = true)
    public int countUnusedBackupCodes(User user) {
        return backupCodeRepository.findByUserAndUsedAtIsNull(user).size();
    }

    public MfaSetupResponseDTO beginEnrollment(User user) {
        if (mfaSecretRepository.existsById(user.getId())) {
            throw new BusinessException("MFA_ERROR", "MFA is already enabled", HttpStatus.CONFLICT);
        }
        String secret = secretGenerator.generate();
        String issuer = oauth2Properties.getIssuerName();
        String accountName = user.getEmail();
        QrData data = new QrData.Builder()
                .label(accountName)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        try {
            String qrUri = data.getUri();
            return new MfaSetupResponseDTO(secret, qrUri, issuer, accountName);
        } catch (Exception e) {
            throw new BusinessException("MFA_ERROR", "Failed to generate MFA setup", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public MfaBackupCodesResponseDTO confirmEnrollment(User user, String plainSecret, String code) {
        if (!codeVerifier.isValidCode(plainSecret, code)) {
            throw new BusinessException("MFA_ERROR", "Invalid verification code", HttpStatus.BAD_REQUEST);
        }
        if (mfaSecretRepository.existsById(user.getId())) {
            throw new BusinessException("MFA_ERROR", "MFA is already enabled", HttpStatus.CONFLICT);
        }
        UserMfaSecret secret = new UserMfaSecret();
        secret.setUser(user);
        secret.setSecretEncrypted(secretEncryptionService.encrypt(plainSecret));
        secret.setEnabledAt(LocalDateTime.now());
        mfaSecretRepository.save(secret);
        return regenerateBackupCodes(user);
    }

    public void disableMfa(User user) {
        mfaSecretRepository.findById(user.getId()).ifPresent(secret -> {
            mfaSecretRepository.delete(secret);
            backupCodeRepository.findByUserAndUsedAtIsNull(user)
                    .forEach(backupCodeRepository::delete);
        });
    }

    public MfaBackupCodesResponseDTO regenerateBackupCodes(User user) {
        if (!mfaSecretRepository.existsById(user.getId())) {
            throw new BusinessException("MFA_ERROR", "Enable MFA before generating backup codes", HttpStatus.BAD_REQUEST);
        }
        backupCodeRepository.findByUserAndUsedAtIsNull(user).forEach(backupCodeRepository::delete);
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            String code = generateBackupCode();
            codes.add(code);
            UserMfaBackupCode entity = new UserMfaBackupCode();
            entity.setUser(user);
            entity.setCodeHash(passwordEncoder.encode(normalizeBackupCode(code)));
            backupCodeRepository.save(entity);
        }
        return new MfaBackupCodesResponseDTO(codes);
    }

    public void verifyLoginCode(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("MFA_ERROR", "User not found", HttpStatus.UNAUTHORIZED));
        if (verifyTotp(user, code) || verifyBackupCode(user, code)) {
            return;
        }
        throw new BusinessException("MFA_ERROR", "Invalid verification code", HttpStatus.UNAUTHORIZED);
    }

    private boolean verifyTotp(User user, String code) {
        if (!code.matches("^[0-9]{6}$")) {
            return false;
        }
        return mfaSecretRepository.findById(user.getId())
                .map(secret -> {
                    String plain = secretEncryptionService.decrypt(secret.getSecretEncrypted());
                    return codeVerifier.isValidCode(plain, code);
                })
                .orElse(false);
    }

    private boolean verifyBackupCode(User user, String code) {
        String normalized = normalizeBackupCode(code);
        for (UserMfaBackupCode backup : backupCodeRepository.findByUserAndUsedAtIsNull(user)) {
            if (passwordEncoder.matches(normalized, backup.getCodeHash())) {
                backup.setUsedAt(LocalDateTime.now());
                backupCodeRepository.save(backup);
                return true;
            }
        }
        return false;
    }

    private String generateBackupCode() {
        int left = secureRandom.nextInt(0x10000);
        int right = secureRandom.nextInt(0x10000);
        return String.format(Locale.ROOT, "%04X-%04X", left, right);
    }

    private String normalizeBackupCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(" ", "");
    }
}
