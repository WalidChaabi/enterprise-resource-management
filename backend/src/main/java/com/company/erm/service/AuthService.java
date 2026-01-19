package com.company.erm.service;

import com.company.erm.dto.AuthRequest;
import com.company.erm.dto.AuthResponse;
import com.company.erm.entity.User;
import com.company.erm.entity.UserRole;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;
import com.company.erm.repository.UserRoleRepository;
import com.company.erm.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("AUTH_ERROR", "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new BusinessException("AUTH_ERROR", "User account is inactive", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_ERROR", "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        List<String> roleNames = userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        String token = tokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getOrganization().getId()
        );

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getOrganization().getId(),
                roleNames
        );
    }
}

