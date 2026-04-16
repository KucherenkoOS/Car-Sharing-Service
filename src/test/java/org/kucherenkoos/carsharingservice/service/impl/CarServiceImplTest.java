package org.kucherenkoos.carsharingservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;
import org.kucherenkoos.carsharingservice.dto.car.CreateCarRequestDto;
import org.kucherenkoos.carsharingservice.dto.car.UpdateCarRequestDto;
import org.kucherenkoos.carsharingservice.exception.EntityNotFoundException;
import org.kucherenkoos.carsharingservice.mapper.CarMapper;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.repository.CarRepository;
import org.kucherenkoos.carsharingservice.util.TestDataHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Create a car with valid DTO")
    void createCar_ValidRequest_ReturnsCarDto() {
        // Given
        CreateCarRequestDto requestDto = TestDataHelper.createTestCreateCarRequestDto();
        Car car = TestDataHelper.createTestCar();
        CarDto expectedDto = TestDataHelper.createTestCarDto();

        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expectedDto);

        // When
        CarDto actualDto = carService.createCar(requestDto);

        // Then
        assertEquals(expectedDto, actualDto);
        verify(carRepository).save(car);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Get car by valid ID")
    void getCarById_ValidId_ReturnsCarDto() {
        // Given
        Long carId = TestDataHelper.CAR_ID;
        Car car = TestDataHelper.createTestCar();
        CarDto expectedDto = TestDataHelper.createTestCarDto();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedDto);

        // When
        CarDto actualDto = carService.getCarById(carId);

        // Then
        assertEquals(expectedDto, actualDto);
    }

    @Test
    @DisplayName("Get car by invalid ID throws EntityNotFoundException")
    void getCarById_InvalidId_ThrowsException() {
        // Given
        Long carId = 100L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.getCarById(carId));

        assertEquals("Can't find car by id: " + carId, exception.getMessage());
        verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Get all cars with pagination")
    void getAll_ReturnsPageOfCarDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Car car = TestDataHelper.createTestCar();
        CarDto carDto = TestDataHelper.createTestCarDto();
        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // When
        Page<CarDto> actualPage = carService.getAll(pageable);

        // Then
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(carDto, actualPage.getContent().get(0));
        verify(carRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Update car with valid ID and DTO")
    void update_ValidIdAndDto_ReturnsUpdatedCarDto() {
        // Given
        Long carId = TestDataHelper.CAR_ID;
        UpdateCarRequestDto requestDto = TestDataHelper.createTestUpdateCarRequestDto();
        Car car = TestDataHelper.createTestCar();

        CarDto expectedDto = TestDataHelper.createTestCarDto();
        expectedDto.setModel(requestDto.getModel());
        expectedDto.setInventory(requestDto.getInventory());
        expectedDto.setDailyFee(requestDto.getDailyFee());

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        when(carMapper.toDto(car)).thenReturn(expectedDto);

        // When
        CarDto actualDto = carService.update(carId, requestDto);

        // Then
        assertEquals(expectedDto, actualDto);
        verify(carRepository).findById(carId);
        verify(carMapper).updateCarFromDto(requestDto, car);
    }

    @Test
    @DisplayName("Update car with invalid ID throws EntityNotFoundException")
    void update_InvalidId_ThrowsException() {
        // Given
        Long carId = 100L;
        UpdateCarRequestDto requestDto = TestDataHelper.createTestUpdateCarRequestDto();

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.update(carId, requestDto));

        assertEquals("Can't find car by id: " + carId, exception.getMessage());
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Delete car by valid ID")
    void deleteById_ValidId_Success() {
        // Given
        Long carId = TestDataHelper.CAR_ID;
        when(carRepository.existsById(carId)).thenReturn(true);

        // When
        carService.deleteById(carId);

        // Then
        verify(carRepository).existsById(carId);
        verify(carRepository).deleteById(carId);
    }

    @Test
    @DisplayName("Delete car by invalid ID throws EntityNotFoundException")
    void deleteById_InvalidId_ThrowsException() {
        // Given
        Long carId = 100L;
        when(carRepository.existsById(carId)).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.deleteById(carId));

        assertEquals("Can't find car by id: " + carId, exception.getMessage());
        verify(carRepository).existsById(carId);
        verifyNoMoreInteractions(carRepository);
    }
}
