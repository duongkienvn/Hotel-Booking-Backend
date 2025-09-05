package com.dev.hotelbooking.controller;

import com.dev.hotelbooking.model.Role;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.FOUND;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final IRoleService roleService;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return new ResponseEntity<>(roleService.getRoles(), FOUND);
    }

    @PostMapping
    public ResponseEntity<String> createRole(@RequestBody Role theRole) {
        roleService.createRole(theRole);
        return ResponseEntity.ok("New role created successfully!");
    }

    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable("roleId") Long roleId) {
        roleService.deleteRole(roleId);
    }

    @DeleteMapping("/{roleId}/users")
    public ResponseEntity<Role> removeAllUsersFromRole(@PathVariable("roleId") Long roleId) {
        return ResponseEntity.ok(roleService.removeAllUsersFromRole(roleId));
    }

    @DeleteMapping("/{roleId}/users/{userId}")
    public ResponseEntity<User> removeUserFromRole(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {
        return ResponseEntity.ok(roleService.removeUserFromRole(userId, roleId));
    }

    @PutMapping("/{roleId}/users/{userId}")
    public ResponseEntity<User> assignRoleToUser(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {
        return ResponseEntity.ok(roleService.assignRoleToUser(userId, roleId));
    }
}
