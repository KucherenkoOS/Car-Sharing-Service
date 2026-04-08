package org.kucherenkoos.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.user.UserLoginRequestDto;
import org.kucherenkoos.carsharingservice.dto.user.UserLoginResponseDto;
import org.kucherenkoos.carsharingservice.dto.user.UserRegistrationRequestDto;
import org.kucherenkoos.carsharingservice.dto.user.UserResponseDto;
import org.kucherenkoos.carsharingservice.exception.RegistrationException;
import org.kucherenkoos.carsharingservice.security.AuthenticationService;
import org.kucherenkoos.carsharingservice.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and authentication")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public UserResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @Operation(summary = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public UserLoginResponseDto login(
            @RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
