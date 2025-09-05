package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.model.Role;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.repository.RoleRepository;
import com.dev.hotelbooking.repository.UserRepository;
import com.dev.hotelbooking.service.IRoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService implements IRoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role createRole(Role theRole) {
        if (roleRepository.existsByName(theRole.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        theRole.setName(theRole.getName().toUpperCase());
        return roleRepository.save(theRole);
    }

    @Override
    public void deleteRole(Long id) {
        this.removeAllUsersFromRole(id);
        roleRepository.deleteById(id);
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    @Override
    public User removeUserFromRole(Long userId, Long roleId) {
        User user = getUserById(userId);
        Role existingRole = getRoleById(roleId);
        if (existingRole.getUsers().contains(user)) {
            existingRole.removeUserFromRole(user);
            roleRepository.save(existingRole);
            return user;
        }
        throw new AppException(ErrorCode.ROLE_NOT_FOUND,
                "User is not yet assigned to the " + existingRole.getName() + " role");
    }

    @Override
    public User assignRoleToUser(Long userId, Long roleId) {
        User user = getUserById(userId);
        Role existingRole = getRoleById(roleId);
        if (user.getRoles().contains(existingRole)) {
            throw new AppException(ErrorCode.ROLE_EXISTED,
                    user.getFirstName() + " is already assigned to the" + existingRole.getName() + " role");
        }
        existingRole.assignRoleToUser(user);
        roleRepository.save(existingRole);
        return user;
    }

    @Override
    public Role removeAllUsersFromRole(Long roleId) {
        Role existingRole = getRoleById(roleId);
        existingRole.removeAllUsersFromRole();
        return roleRepository.save(existingRole);
    }

    private Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
