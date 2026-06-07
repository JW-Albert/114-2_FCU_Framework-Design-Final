package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.AdminCreateUserRequest;
import com.vehicle.management.api.dto.ChangeRoleRequest;
import com.vehicle.management.api.dto.UpdateUserRequest;
import com.vehicle.management.api.dto.UserResponse;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> listAll(@AuthenticationPrincipal UserDetails principal) {
        User caller = userService.findByEmail(principal.getUsername());
        return userService.listAllUsers(caller).stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getOne(@AuthenticationPrincipal UserDetails principal,
                               @PathVariable UUID id) {
        User caller = userService.findByEmail(principal.getUsername());
        return UserResponse.from(userService.getUserById(caller, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal UserDetails principal,
                               @Valid @RequestBody AdminCreateUserRequest req) {
        User caller = userService.findByEmail(principal.getUsername());
        User created = userService.createUserByAdmin(caller, req.name(), req.email(),
                req.password(), req.role());
        return UserResponse.from(created);
    }

    @PutMapping("/{id}")
    public UserResponse update(@AuthenticationPrincipal UserDetails principal,
                               @PathVariable UUID id,
                               @Valid @RequestBody UpdateUserRequest req) {
        User caller = userService.findByEmail(principal.getUsername());
        return UserResponse.from(userService.updateUser(caller, id, req.name(), req.email()));
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@AuthenticationPrincipal UserDetails principal,
                                   @PathVariable UUID id,
                                   @Valid @RequestBody ChangeRoleRequest req) {
        User caller = userService.findByEmail(principal.getUsername());
        return UserResponse.from(userService.changeUserRole(caller, id, req.role()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable UUID id) {
        User caller = userService.findByEmail(principal.getUsername());
        userService.deleteUser(caller, id);
    }
}
