package com.virtbank.service;

import com.virtbank.config.Audited;
import com.virtbank.dto.UserResponse;
import com.virtbank.entity.Role;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.UserStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.RoleRepository;
import com.virtbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    @Audited(action = "UPDATE_USER_ROLE", entityType = "User")
    @Transactional
    public UserResponse updateUserRole(Long userId, String roleName) {
        User user = findUserOrThrow(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.setRoles(Set.of(role));
        return toResponse(userRepository.save(user));
    }

    @Audited(action = "UPDATE_USER_STATUS", entityType = "User")
    @Transactional
    public UserResponse updateUserStatus(Long userId, String status) {
        User user = findUserOrThrow(userId);
        user.setStatus(UserStatus.valueOf(status.toUpperCase()));
        return toResponse(userRepository.save(user));
    }

    @Audited(action = "DELETE_USER", entityType = "User")
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserOrThrow(userId);
        userRepository.delete(user);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId()).email(u.getEmail())
                .firstName(u.getFirstName()).lastName(u.getLastName())
                .phone(u.getPhone())
                .userType(u.getUserType().name())
                .status(u.getStatus().name())
                .emailVerified(u.getEmailVerified())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
