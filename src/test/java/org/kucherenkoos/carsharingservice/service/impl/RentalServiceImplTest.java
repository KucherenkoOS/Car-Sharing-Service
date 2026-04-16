package org.kucherenkoos.carsharingservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.event.rental.RentalCreatedEvent;
import org.kucherenkoos.carsharingservice.mapper.RentalMapper;
import org.kucherenkoos.carsharingservice.model.*;
import org.kucherenkoos.carsharingservice.repository.CarRepository;
import org.kucherenkoos.carsharingservice.repository.PaymentRepository;
import org.kucherenkoos.carsharingservice.repository.RentalRepository;
import org.kucherenkoos.carsharingservice.service.UserService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private User regularUser;
    private User managerUser;
    private Car availableCar;
    private CreateRentalRequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        regularUser = createUser(1L, "ROLE_USER");
        managerUser = createUser(2L, "ROLE_MANAGER");

        availableCar = new Car();
        availableCar.setId(10L);
        availableCar.setInventory(2);

        validRequestDto = new CreateRentalRequestDto();
        validRequestDto.setCarId(10L);
        validRequestDto.setRentalDate(LocalDate.now());
        validRequestDto.setReturnDate(LocalDate.now().plusDays(3));
    }

    @Test
    @DisplayName("Create rental - Success path")
    void createRental_ValidRequest_Success() {
        // Given
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(regularUser.getId())).thenReturn(false);
        when(paymentRepository.existsByRentalUserIdAndStatusNot(regularUser.getId(), Payment.PaymentStatus.PAID)).thenReturn(false);
        when(carRepository.findById(availableCar.getId())).thenReturn(Optional.of(availableCar));

        Rental mappedRental = new Rental();
        Rental savedRental = new Rental();
        RentalResponseDto expectedDto = new RentalResponseDto();

        when(rentalMapper.toEntity(validRequestDto)).thenReturn(mappedRental);
        when(rentalRepository.save(mappedRental)).thenReturn(savedRental);
        when(rentalMapper.toDto(savedRental)).thenReturn(expectedDto);

        // When
        RentalResponseDto actualDto = rentalService.createRental(validRequestDto);

        // Then
        assertEquals(expectedDto, actualDto);
        assertEquals(1, availableCar.getInventory(), "Inventory should be decreased by 1");

        verify(carRepository).save(availableCar);
        verify(rentalRepository).save(mappedRental);
        verify(eventPublisher).publishEvent(any(RentalCreatedEvent.class));
    }

    @Test
    @DisplayName("Create rental - Fails when dates are invalid")
    void createRental_InvalidDates_ThrowsIllegalArgumentException() {
        // Given
        validRequestDto.setReturnDate(LocalDate.now().minusDays(1));

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> rentalService.createRental(validRequestDto));
        assertEquals("Return date cannot be before rental date", exception.getMessage());
    }

    @Test
    @DisplayName("Create rental - Fails when user has active rental")
    void createRental_ActiveRentalExists_ThrowsIllegalStateException() {
        // Given
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(regularUser.getId())).thenReturn(true);

        // When & Then
        Exception exception = assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(validRequestDto));
        assertEquals("You cannot create a new rental. Please return your current car first.", exception.getMessage());
    }

    @Test
    @DisplayName("Create rental - Fails when user has unpaid payments")
    void createRental_UnpaidPaymentsExist_ThrowsIllegalStateException() {
        // Given
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(regularUser.getId())).thenReturn(false);
        when(paymentRepository.existsByRentalUserIdAndStatusNot(regularUser.getId(), Payment.PaymentStatus.PAID)).thenReturn(true);

        // When & Then
        Exception exception = assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(validRequestDto));
        assertEquals("You have unpaid rentals. Please pay them before renting a new car.", exception.getMessage());
    }

    @Test
    @DisplayName("Create rental - Fails when car is out of stock")
    void createRental_CarOutOfStock_ThrowsIllegalStateException() {
        // Given
        availableCar.setInventory(0);
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(rentalRepository.existsByUserIdAndActualReturnDateIsNull(regularUser.getId())).thenReturn(false);
        when(paymentRepository.existsByRentalUserIdAndStatusNot(regularUser.getId(), Payment.PaymentStatus.PAID)).thenReturn(false);
        when(carRepository.findById(availableCar.getId())).thenReturn(Optional.of(availableCar));

        // When & Then
        Exception exception = assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(validRequestDto));
        assertEquals("Car is out of stock", exception.getMessage());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get rentals - As MANAGER (can search by any userId)")
    void getRentals_AsManager_UsesRequestedUserId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Long searchUserId = 99L;

        when(userService.getCurrentUser()).thenReturn(managerUser);

        Rental rental = new Rental();
        RentalResponseDto dto = new RentalResponseDto();
        Page<Rental> rentalPage = new PageImpl<>(List.of(rental));

        when(rentalRepository.findFilteredRentals(searchUserId, true, pageable)).thenReturn(rentalPage);
        when(rentalMapper.toDto(rental)).thenReturn(dto);

        // When
        Page<RentalResponseDto> result = rentalService.getRentals(searchUserId, true, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        verify(rentalRepository).findFilteredRentals(searchUserId, true, pageable);
    }

    @Test
    @DisplayName("Get rentals - As USER (forces current user ID)")
    void getRentals_AsUser_ForcesCurrentUserId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Long requestedUserId = 99L;

        when(userService.getCurrentUser()).thenReturn(regularUser);

        Page<Rental> rentalPage = new PageImpl<>(List.of());
        when(rentalRepository.findFilteredRentals(regularUser.getId(), false, pageable)).thenReturn(rentalPage);

        // When
        rentalService.getRentals(requestedUserId, false, pageable);

        // Then
        verify(rentalRepository).findFilteredRentals(regularUser.getId(), false, pageable);
    }

    @Test
    @DisplayName("Get rental by ID - As OWNER - Success")
    void getRentalById_AsOwner_Success() {
        // Given
        Long rentalId = 1L;
        Rental rental = new Rental();
        rental.setUser(regularUser);

        RentalDetailDto detailDto = new RentalDetailDto();

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(rentalMapper.toDetailDto(rental)).thenReturn(detailDto);

        // When
        RentalDetailDto actualDto = rentalService.getRentalById(rentalId);

        // Then
        assertEquals(detailDto, actualDto);
    }

    @Test
    @DisplayName("Get rental by ID - NOT owner and NOT manager - Throws AccessDeniedException")
    void getRentalById_NotOwner_ThrowsException() {
        // Given
        Long rentalId = 1L;
        Rental rental = new Rental();
        User anotherUser = createUser(99L, "ROLE_USER");
        rental.setUser(anotherUser);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userService.getCurrentUser()).thenReturn(regularUser);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> rentalService.getRentalById(rentalId));
    }

    @Test
    @DisplayName("Return rental - Success")
    void returnRental_ValidRequest_Success() {
        // Given
        Long rentalId = 1L;
        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(regularUser);
        rental.setCar(availableCar);
        rental.setActualReturnDate(null);

        RentalResponseDto expectedDto = new RentalResponseDto();

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        // When
        RentalResponseDto actualDto = rentalService.returnRental(rentalId);

        // Then
        assertEquals(expectedDto, actualDto);
        assertNotNull(rental.getActualReturnDate(), "Actual return date should be set");
        assertEquals(3, availableCar.getInventory(), "Inventory should be increased by 1 (from 2 to 3)");

        verify(carRepository).save(availableCar);
        verify(rentalRepository).save(rental);
    }

    @Test
    @DisplayName("Return rental - Fails when already returned")
    void returnRental_AlreadyReturned_ThrowsIllegalStateException() {
        // Given
        Long rentalId = 1L;
        Rental rental = new Rental();
        rental.setActualReturnDate(LocalDate.now().minusDays(1));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        // When & Then
        Exception exception = assertThrows(IllegalStateException.class, () -> rentalService.returnRental(rentalId));
        assertEquals("Rental was already returned", exception.getMessage());
    }

    private User createUser(Long id, String roleName) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@example.com");

        Role role = new Role();
        role.setName(RoleName.valueOf(roleName));
        user.setRoles(Set.of(role));

        return user;
    }
}
