package com.vehicle.management.service;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.domain.role.Role;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.repository.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
        Role role = RoleFactory.create(roleName);
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

    public List<User> listAllUsers(User caller) {
        requireManageUser(caller);
        return userRepo.findAll();
    }

    public User getUserById(User caller, UUID targetId) {
        requireManageUser(caller);
        return findById(targetId);
    }

    public User createUserByAdmin(User caller, String name, String email,
                                  String rawPassword, String roleName) {
        requireManageUser(caller);
        return register(name, email, rawPassword, roleName);
    }

    public User updateUser(User caller, UUID targetId, String name, String email) {
        requireManageUser(caller);
        User target = findById(targetId);
        if (!target.getEmail().equalsIgnoreCase(email) && userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        User updated = new User(target.getId(), name, email,
                target.getPasswordHash(), target.getRoles(), target.getCreatedAt());
        return userRepo.save(updated);
    }

    public User changeUserRole(User caller, UUID targetId, String roleName) {
        requireManageUser(caller);
        User target = findById(targetId);
        Role newRole = RoleFactory.create(roleName);
        User updated = new User(target.getId(), target.getName(), target.getEmail(),
                target.getPasswordHash(), Set.of(newRole), target.getCreatedAt());
        return userRepo.save(updated);
    }

    public void deleteUser(User caller, UUID targetId) {
        requireManageUser(caller);
        findById(targetId);
        userRepo.deleteById(targetId);
    }

    private void requireManageUser(User caller) {
        if (!caller.can(Permission.MANAGE_USER)) {
            throw new PermissionDeniedException("Only admins can manage user accounts");
        }
    }
}
