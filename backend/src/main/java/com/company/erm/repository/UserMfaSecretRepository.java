package com.company.erm.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.UserMfaSecret;

public interface UserMfaSecretRepository extends JpaRepository<UserMfaSecret, UUID> {
}
