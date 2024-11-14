package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.models.User;
import com.user_management_service.user_management_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        validateUserStatus(user);

        return buildUserDetails(user);
    }

    private void validateUserStatus(User user) {
        if (!user.isActive()) {
            log.warn("Attempted to load inactive user: {}", user.getEmail());
            throw new DisabledException("User account is not active");
        }

        if (!user.isEmailVerified()) {
            log.warn("Attempted to load unverified user: {}", user.getEmail());
            throw new LockedException("Email address not verified");
        }

        // Optional: Check for account inactivity
        if (isAccountInactive(user)) {
            log.warn("Attempted to load inactive account: {}", user.getEmail());
            throw new DisabledException("Account has been inactive for too long");
        }
    }

    private boolean isAccountInactive(User user) {
        if (user.getLastLogin() == null) {
            return false;
        }
        // Consider account inactive if no login for 90 days
        return user.getLastLogin().plusDays(90).isBefore(LocalDateTime.now());
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(!user.isEmailVerified())
                .authorities(getAuthorities(user))
                .build();
    }

    private Set<GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add basic user role
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Add department-based authority
        if (user.getDepartment() != null) {
            authorities.add(new SimpleGrantedAuthority(
                    "DEPARTMENT_" + user.getDepartment().getName().toUpperCase()
            ));
        }

        // Add role-based authority if you have roles defined in your User entity

        // Optional: Add special authorities based on user properties
        if (isSpecialUser(user)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SPECIAL"));
        }

        return authorities;
    }

    private boolean isSpecialUser(User user) {
        // Implement your logic to determine if a user should have special privileges
        // For example, based on email domain, department, or other criteria
        return false;
    }

    /**
     * Helper method to get authorities as a list of strings
     * Useful for logging or debugging purposes
     */
    public List<String> getUserAuthorities(String email) {
        try {
            UserDetails userDetails = loadUserByUsername(email);
            return userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        } catch (UsernameNotFoundException e) {
            log.warn("Failed to get authorities for non-existent user: {}", email);
            return Collections.emptyList();
        }
    }

    /**
     * Helper method to check if a user has a specific authority
     */
    public boolean hasAuthority(String email, String authority) {
        try {
            UserDetails userDetails = loadUserByUsername(email);
            return userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(authority));
        } catch (UsernameNotFoundException e) {
            log.warn("Failed to check authority for non-existent user: {}", email);
            return false;
        }
    }

    /**
     * Helper method to check if an email exists and user is active
     */

}