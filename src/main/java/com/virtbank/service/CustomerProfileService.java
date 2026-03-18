package com.virtbank.service;

import com.virtbank.dto.ChangePasswordRequest;
import com.virtbank.dto.ProfileResponse;
import com.virtbank.dto.UpdateProfileRequest;
import com.virtbank.entity.User;
import com.virtbank.entity.UserProfile;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.UserProfileRepository;
import com.virtbank.repository.UserRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getMyProfile() {
        User user = securityUtils.getCurrentUser();
        UserProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return toResponse(user, profile);
    }

    @Transactional
    public ProfileResponse updateMyProfile(UpdateProfileRequest req) {
        User user = securityUtils.getCurrentUser();

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        userRepository.save(user);

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(UserProfile.builder().user(user).build());
        if (req.getAddressLine1() != null) profile.setAddressLine1(req.getAddressLine1());
        if (req.getAddressLine2() != null) profile.setAddressLine2(req.getAddressLine2());
        if (req.getCity() != null) profile.setCity(req.getCity());
        if (req.getState() != null) profile.setState(req.getState());
        if (req.getZipCode() != null) profile.setZipCode(req.getZipCode());
        if (req.getCountry() != null) profile.setCountry(req.getCountry());
        if (req.getDateOfBirth() != null) profile.setDateOfBirth(req.getDateOfBirth());
        profileRepository.save(profile);

        return toResponse(user, profile);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        User user = securityUtils.getCurrentUser();
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private ProfileResponse toResponse(User u, UserProfile p) {
        ProfileResponse.ProfileResponseBuilder b = ProfileResponse.builder()
                .userId(u.getId()).firstName(u.getFirstName())
                .lastName(u.getLastName()).email(u.getEmail()).phone(u.getPhone());
        if (p != null) {
            b.id(p.getId()).addressLine1(p.getAddressLine1())
                    .addressLine2(p.getAddressLine2()).city(p.getCity())
                    .state(p.getState()).zipCode(p.getZipCode())
                    .country(p.getCountry()).dateOfBirth(p.getDateOfBirth())
                    .profilePictureUrl(p.getProfilePictureUrl());
        }
        return b.build();
    }
}
