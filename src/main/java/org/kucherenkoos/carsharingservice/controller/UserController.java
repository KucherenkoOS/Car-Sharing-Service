package org.kucherenkoos.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.user.UpdateUserProfileDto;
import org.kucherenkoos.carsharingservice.dto.user.UpdateUserRoleRequestDto;
import org.kucherenkoos.carsharingservice.dto.user.UserResponseDto;
import org.kucherenkoos.carsharingservice.model.User;
import org.kucherenkoos.carsharingservice.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User management", description = "Endpoints for managing users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user profile info")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public UserResponseDto getMyProfile(
            @AuthenticationPrincipal User user) {
        return userService.getByEmail(user.getEmail());
    }

    @Operation(summary = "Update user profile info")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me")
    public UserResponseDto patchMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserProfileDto requestDto) {
        return userService.patchProfile(user.getEmail(), requestDto);
    }

    @Operation(summary = "Update role for user (Manager operation)")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/role")
    public UserResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRoleRequestDto requestDto) {
        return userService.updateRole(id, requestDto.getRole());
    }
}
