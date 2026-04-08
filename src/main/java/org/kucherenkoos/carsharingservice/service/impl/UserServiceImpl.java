package org.kucherenkoos.carsharingservice.service.impl;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.user.UpdateUserProfileDto;
import org.kucherenkoos.carsharingservice.dto.user.UserRegistrationRequestDto;
import org.kucherenkoos.carsharingservice.dto.user.UserResponseDto;
import org.kucherenkoos.carsharingservice.exception.EntityNotFoundException;
import org.kucherenkoos.carsharingservice.exception.RegistrationException;
import org.kucherenkoos.carsharingservice.mapper.UserMapper;
import org.kucherenkoos.carsharingservice.model.Role;
import org.kucherenkoos.carsharingservice.model.RoleName;
import org.kucherenkoos.carsharingservice.model.User;
import org.kucherenkoos.carsharingservice.repository.RoleRepository;
import org.kucherenkoos.carsharingservice.repository.UserRepository;
import org.kucherenkoos.carsharingservice.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("User with this email already exists");
        }

        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RegistrationException(RoleName.ROLE_USER + " not found"));

        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getByEmail(String email) {
        User user = getUserByEmailOrThrow(email);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto patchProfile(String email, UpdateUserProfileDto dto) {
        User user = getUserByEmailOrThrow(email);

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto updateRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        String fullRoleName = "ROLE_" + role.toUpperCase();
        RoleName roleName;
        try {
            roleName = RoleName.valueOf(fullRoleName);
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Invalid role: " + role);
        }

        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException(roleName + " not found"));

        user.setRoles(new HashSet<>(Set.of(newRole)));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with email: " + email));
    }

}
