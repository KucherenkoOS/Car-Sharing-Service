package org.kucherenkoos.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.util.TestDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Save a new car to the database")
    void save_ValidCar_ReturnsSavedCar() {
        // Given
        Car car = TestDataHelper.createTestCar();
        car.setId(null);

        // When
        Car savedCar = carRepository.save(car);

        // Then
        assertNotNull(savedCar.getId());
        assertEquals(car.getModel(), savedCar.getModel());
        assertEquals(car.getBrand(), savedCar.getBrand());
    }

    @Test
    @DisplayName("Verify soft delete mechanism (@SQLDelete and @SQLRestriction)")
    void deleteById_SoftDelete_CarIsHidden() {
        // Given
        Car car = TestDataHelper.createTestCar();
        car.setId(null);
        Car savedCar = carRepository.save(car);
        Long carId = savedCar.getId();

        assertTrue(carRepository.findById(carId).isPresent());

        // When
        carRepository.deleteById(carId);

        // Then
        Optional<Car> deletedCar = carRepository.findById(carId);
        assertTrue(deletedCar.isEmpty(), "Car should be hidden after soft delete");
    }
}
