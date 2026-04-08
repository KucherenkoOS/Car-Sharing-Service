package org.kucherenkoos.carsharingservice.service;

import org.kucherenkoos.carsharingservice.dto.user.UpdateUserProfileDto;
import org.kucherenkoos.carsharingservice.dto.user.UserRegistrationRequestDto;
import org.kucherenkoos.carsharingservice.dto.user.UserResponseDto;
import org.kucherenkoos.carsharingservice.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException;

    UserResponseDto getByEmail(String email);

    UserResponseDto patchProfile(String email, UpdateUserProfileDto dto);

    UserResponseDto updateRole(Long id, String role);
}
