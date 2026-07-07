package com.company.erm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.erm.entity.User;
import com.company.erm.entity.UserIdentityLink;

public interface UserIdentityLinkRepository extends JpaRepository<UserIdentityLink, UUID> {

    Optional<UserIdentityLink> findByProviderAndSubject(String provider, String subject);

    List<UserIdentityLink> findByUser(User user);
}
