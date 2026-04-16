package org.kucherenkoos.carsharingservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.model.User;

class RentalMapperTest {

    private final RentalMapper rentalMapper = new RentalMapperImpl();

    @Test
    @DisplayName("Map Rental entity to RentalResponseDto (extracts carId and userId)")
    void toDto_ValidRental_ReturnsDtoWithNestedIds() {
        // Given
        Car car = new Car();
        car.setId(10L);

        User user = new User();
        user.setId(20L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(5));
        rental.setCar(car);
        rental.setUser(user);

        // When
        RentalResponseDto dto = rentalMapper.toDto(rental);

        // Then
        assertNotNull(dto);
        assertEquals(rental.getId(), dto.getId());
        assertEquals(rental.getRentalDate(), dto.getRentalDate());
        assertEquals(rental.getReturnDate(), dto.getReturnDate());

        assertEquals(10L, dto.getCarId());

        assertEquals(20L, dto.getUserId());
    }

    @Test
    @DisplayName("Map Rental entity to RentalDetailDto")
    void toDetailDto_ValidRental_ReturnsDetailDto() {
        // Given
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.now());

        // When
        RentalDetailDto dto = rentalMapper.toDetailDto(rental);

        // Then
        assertNotNull(dto);
        assertEquals(rental.getId(), dto.getId());
        assertEquals(rental.getRentalDate(), dto.getRentalDate());
    }

    @Test
    @DisplayName("Map CreateRentalRequestDto to Rental entity (ignores specific fields)")
    void toEntity_ValidDto_IgnoresSpecifiedFields() {
        // Given
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();
        requestDto.setRentalDate(LocalDate.now());
        requestDto.setReturnDate(LocalDate.now().plusDays(3));

        // When
        Rental rental = rentalMapper.toEntity(requestDto);

        // Then
        assertNotNull(rental);
        assertEquals(requestDto.getRentalDate(), rental.getRentalDate());
        assertEquals(requestDto.getReturnDate(), rental.getReturnDate());

        assertNull(rental.getId(), "ID must be ignored");
        assertNull(rental.getActualReturnDate(), "Actual return date must be null when creating a new Rental");
        assertNull(rental.getUser(), "User must be inserted by service when creating a new Rental");
        assertNull(rental.getCar(), "Car must be inserted by service when creating a new Rental");
    }
}