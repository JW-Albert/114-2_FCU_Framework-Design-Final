package com.vehicle.management.service;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Role;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.repository.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final IUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(IUserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String name, String email, String rawPassword, String roleName) {
        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        Role role = RoleFactory.create(roleName);   // Factory Method (Ch11)
        User user = new User(UUID.randomUUID(), name, email,
                passwordEncoder.encode(rawPassword), Set.of(role), Instant.now());
        return userRepo.save(user);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public User findById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
