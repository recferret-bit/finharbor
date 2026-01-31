package com.casino.users.service;

import com.casino.users.dto.CreateUserRequest;
import com.casino.users.dto.UpdateUserRequest;
import com.casino.users.dto.UserResponse;
import com.casino.users.entity.User;
import com.casino.users.event.UserCreatedEvent;
import com.casino.users.event.UserUpdatedEvent;
import com.casino.users.exception.ResourceNotFoundException;
import com.casino.users.exception.DuplicateResourceException;
import com.casino.users.mapper.UserMapper;
import com.casino.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, String emailFilter) {
        if (emailFilter != null && !emailFilter.isBlank()) {
            return userRepository.findByEmailContainingIgnoreCase(emailFilter, pageable)
                    .map(userMapper::toResponse);
        }
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email already exists: " + request.getEmail());
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User with username already exists: " + request.getUsername());
        }

        // Create user entity
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(User.UserStatus.ACTIVE)
                .kycStatus(User.KycStatus.NOT_VERIFIED)
                .build();

        user = userRepository.save(user);
        log.info("Created user with id: {}", user.getId());

        // Publish user created event
        publishUserCreatedEvent(user);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getKycStatus() != null) {
            user.setKycStatus(request.getKycStatus());
        }

        user = userRepository.save(user);
        log.info("Updated user with id: {}", user.getId());

        // Publish user updated event
        publishUserUpdatedEvent(user);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("Deleted user with id: {}", userId);
    }

    private void publishUserCreatedEvent(User user) {
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .status(user.getStatus().name())
                    .kycStatus(user.getKycStatus().name())
                    .createdAt(user.getCreatedAt())
                    .build();

            kafkaTemplate.send("user-events", user.getId().toString(), event);
            log.debug("Published UserCreatedEvent for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for user: {}", user.getId(), e);
            // Don't fail the transaction if event publishing fails
        }
    }

    private void publishUserUpdatedEvent(User user) {
        try {
            UserUpdatedEvent event = UserUpdatedEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .status(user.getStatus().name())
                    .kycStatus(user.getKycStatus().name())
                    .updatedAt(user.getUpdatedAt())
                    .build();

            kafkaTemplate.send("user-events", user.getId().toString(), event);
            log.debug("Published UserUpdatedEvent for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to publish UserUpdatedEvent for user: {}", user.getId(), e);
            // Don't fail the transaction if event publishing fails
        }
    }
}
