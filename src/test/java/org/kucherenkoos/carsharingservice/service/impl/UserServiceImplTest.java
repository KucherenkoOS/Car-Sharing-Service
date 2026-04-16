package org.kucherenkoos.carsharingservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role userRole;
    private Role managerRole;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleName.ROLE_USER);

        managerRole = new Role();
        managerRole.setId(2L);
        managerRole.setName(RoleName.ROLE_MANAGER);

        userResponseDto = new UserResponseDto();
        userResponseDto.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Register: Success valid user")
    void register_ValidRequest_Success() {
        // Given
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("rawPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        User unmappedUser = new User();
        unmappedUser.setPassword("rawPassword");
        when(userMapper.toModel(request)).thenReturn(unmappedUser);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.register(request);

        // Then
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("rawPassword");
        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(Set.of(userRole), savedUser.getRoles());
    }

    @Test
    @DisplayName("Register: Fail if email already exists")
    void register_EmailExists_ThrowsRegistrationException() {
        // Given
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userService.register(request));
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get by email: Returns user DTO if found")
    void getByEmail_ExistingEmail_ReturnsDto() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.getByEmail(user.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(userResponseDto.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Patch profile: Updates fields correctly")
    void patchProfile_ValidUpdates_SavesUpdatedUser() {
        // Given
        UpdateUserProfileDto updateDto = new UpdateUserProfileDto();
        updateDto.setFirstName("NewName");
        updateDto.setPassword("newRawPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newRawPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // When
        userService.patchProfile(user.getEmail(), updateDto);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals("NewName", updatedUser.getFirstName());
        assertEquals("Doe", updatedUser.getLastName());
        assertEquals("newEncodedPassword", updatedUser.getPassword());
    }

    @Test
    @DisplayName("Update role: Successfully sets new role")
    void updateRole_ValidRole_Success() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_MANAGER)).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // When
        userService.updateRole(user.getId(), "manager");

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(1, savedUser.getRoles().size());
        assertEquals(RoleName.ROLE_MANAGER, savedUser.getRoles().iterator().next().getName());
    }

    @Test
    @DisplayName("Update role: Throws Exception for invalid role string")
    void updateRole_InvalidRoleString_ThrowsEntityNotFoundException() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateRole(user.getId(), "HACKER_ROLE"));
        assertEquals("Invalid role: HACKER_ROLE", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get current user: Returns authenticated user")
    void getCurrentUser_AuthenticatedSession_ReturnsUser() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        User currentUser = userService.getCurrentUser();

        // Then
        assertNotNull(currentUser);
        assertEquals("test@example.com", currentUser.getEmail());
    }

    @Test
    @DisplayName("Get current user: Throws Exception if no authentication")
    void getCurrentUser_NoAuthentication_ThrowsIllegalStateException() {
        // Given
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());
    }
}
