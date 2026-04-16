package org.kucherenkoos.carsharingservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;
import org.kucherenkoos.carsharingservice.dto.car.CreateCarRequestDto;
import org.kucherenkoos.carsharingservice.dto.car.UpdateCarRequestDto;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.util.TestDataHelper;

class CarMapperTest {

    private final CarMapper carMapper = new CarMapperImpl();

    @Test
    @DisplayName("Map CreateCarRequestDto to Car entity")
    void toEntity_ValidCreateDto_ReturnsCar() {
        // Given
        CreateCarRequestDto dto = TestDataHelper.createTestCreateCarRequestDto();

        // When
        Car car = carMapper.toEntity(dto);

        // Then
        assertNull(car.getId(), "ID must not ignore (be null)");
        assertFalse(car.isDeleted(), "Field isDeleted must be ignored (stay false)");

        assertEquals(dto.getModel(), car.getModel());
        assertEquals(dto.getBrand(), car.getBrand());
        assertEquals(dto.getCarType(), car.getCarType());
        assertEquals(dto.getInventory(), car.getInventory());
        assertEquals(dto.getDailyFee(), car.getDailyFee());
    }

    @Test
    @DisplayName("Map Car entity to CarDto")
    void toDto_ValidCar_ReturnsCarDto() {
        // Given
        Car car = TestDataHelper.createTestCar();

        // When
        CarDto dto = carMapper.toDto(car);

        // Then
        assertEquals(car.getId(), dto.getId());
        assertEquals(car.getModel(), dto.getModel());
        assertEquals(car.getBrand(), dto.getBrand());
        assertEquals(car.getCarType().name(), dto.getCarType());
        assertEquals(car.getInventory(), dto.getInventory());
        assertEquals(car.getDailyFee(), dto.getDailyFee());
    }

    @Test
    @DisplayName("Update Car from DTO with all fields present")
    void updateCarFromDto_AllFieldsPresent_UpdatesCar() {
        // Given
        Car car = TestDataHelper.createTestCar();
        UpdateCarRequestDto updateDto = TestDataHelper.createTestUpdateCarRequestDto();

        // When
        carMapper.updateCarFromDto(updateDto, car);

        // Then
        assertEquals(TestDataHelper.CAR_ID, car.getId(), "ID must not change");
        assertFalse(car.isDeleted(), "isDeleted must not change");

        assertEquals(updateDto.getModel(), car.getModel());
        assertEquals(updateDto.getBrand(), car.getBrand());
        assertEquals(updateDto.getCarType(), car.getCarType());
        assertEquals(updateDto.getInventory(), car.getInventory());
        assertEquals(updateDto.getDailyFee(), car.getDailyFee());
    }

    @Test
    @DisplayName("Update Car from DTO with partial fields (ignore nulls)")
    void updateCarFromDto_PartialFields_IgnoresNulls() {
        // Given
        Car car = TestDataHelper.createTestCar();

        UpdateCarRequestDto updateDto = new UpdateCarRequestDto();
        updateDto.setDailyFee(BigDecimal.valueOf(999.99));

        String oldModel = car.getModel();
        String oldBrand = car.getBrand();

        // When
        carMapper.updateCarFromDto(updateDto, car);

        // Then
        assertEquals(BigDecimal.valueOf(999.99), car.getDailyFee(), "Fee must stay");

        assertEquals(oldModel, car.getModel(), "Model must stay, because null");
        assertEquals(oldBrand, car.getBrand(), "Brand must stay, because null");
    }
}
