-- MFA secrets used legacy XOR obfuscation; users must re-enroll after AES-GCM migration.
DELETE FROM erm.user_mfa_backup_codes;
DELETE FROM erm.user_mfa_secrets;
