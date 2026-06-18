package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Role;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.infrastructure.persistence.entity.UserEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaUserRepo;
import com.vehicle.management.repository.IUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Adapter (Ch14): 使用 RoleFactory (Ch11) 還原 User 的 Role 集合。 */
@Repository
public class UserRepositoryAdapter implements IUserRepository {

    private final JpaUserRepo jpa;

    public UserRepositoryAdapter(JpaUserRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        return toDomain(jpa.save(entity));
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private User toDomain(UserEntity e) {
        Set<Role> roles = e.getRoleNames().stream()
                .map(RoleFactory::create)   // Factory Method (Ch11)
                .collect(Collectors.toSet());
        return new User(e.getId(), e.getName(), e.getEmail(),
                e.getPasswordHash(), roles, e.getCreatedAt(), e.getDepartment());
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setName(u.getName());
        e.setEmail(u.getEmail());
        e.setPasswordHash(u.getPasswordHash());
        e.setRoleNames(u.getRoles().stream()
                .map(Role::getName).collect(Collectors.toSet()));
        e.setCreatedAt(u.getCreatedAt());
        e.setDepartment(u.getDepartment());
        return e;
    }
}
