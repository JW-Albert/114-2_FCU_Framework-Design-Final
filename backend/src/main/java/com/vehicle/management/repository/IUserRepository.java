package com.vehicle.management.repository;

import com.vehicle.management.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
    List<User> findAll();
    void deleteById(UUID id);
}
