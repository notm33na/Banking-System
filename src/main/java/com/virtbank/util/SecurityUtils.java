package com.virtbank.util;

import com.virtbank.entity.User;
import com.virtbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get the email of the currently authenticated user from SecurityContext.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new RuntimeException("No authenticated user found in SecurityContext");
    }

    /**
     * Load the full User entity for the currently authenticated user.
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

    /**
     * Get the ID of the currently authenticated user.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Assert that the given resourceOwnerId matches the currently authenticated user.
     * Throws UnauthorizedAccessException if not.
     */
    public void assertOwnership(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(resourceOwnerId)) {
            throw new com.virtbank.exception.UnauthorizedAccessException(
                    "You do not have permission to access this resource");
        }
    }
}
