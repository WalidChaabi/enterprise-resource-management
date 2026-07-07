package com.company.erm.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.User;
import com.company.erm.entity.UserMfaBackupCode;

public interface UserMfaBackupCodeRepository extends JpaRepository<UserMfaBackupCode, UUID> {

    List<UserMfaBackupCode> findByUserAndUsedAtIsNull(User user);
}
