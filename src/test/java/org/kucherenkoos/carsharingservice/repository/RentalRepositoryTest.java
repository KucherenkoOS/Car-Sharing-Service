package org.kucherenkoos.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.CarType;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User activeUser;
    private User anotherUser;
    private Car testCar;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setEmail("active@example.com");
        activeUser.setPassword("password");
        activeUser.setFirstName("John");
        activeUser.setLastName("Doe");
        activeUser = entityManager.persist(activeUser);

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setFirstName("Jane");
        anotherUser.setLastName("Smith");
        anotherUser = entityManager.persist(anotherUser);

        testCar = new Car();
        testCar.setModel("Model S");
        testCar.setBrand("Tesla");
        testCar.setCarType(CarType.SEDAN);
        testCar.setInventory(5);
        testCar.setDailyFee(BigDecimal.valueOf(100));
        testCar = entityManager.persist(testCar);
    }

    @Test
    @DisplayName("Find filtered rentals: by active status")
    void findFilteredRentals_IsActiveTrue_ReturnsOnlyActiveRentals() {
        // Given
        createRental(activeUser, testCar, LocalDate.now(), null);
        createRental(activeUser, testCar, LocalDate.now().minusDays(5), LocalDate.now());

        // When
        Page<Rental> result = rentalRepository.findFilteredRentals(null, true, PageRequest.of(0, 10));

        // Then
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Find filtered rentals: by user ID and inactive status")
    void findFilteredRentals_ByUserIdAndInactive_ReturnsMatchingRentals() {
        // Given
        createRental(activeUser, testCar, LocalDate.now(), null);
        createRental(activeUser, testCar, LocalDate.now().minusDays(5), LocalDate.now());
        createRental(anotherUser, testCar, LocalDate.now().minusDays(5), LocalDate.now());

        // When
        Page<Rental> result = rentalRepository.findFilteredRentals(activeUser.getId(), false, PageRequest.of(0, 10));

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(activeUser.getId(), result.getContent().get(0).getUser().getId());
        assertNotNull(result.getContent().get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Find overdue rentals (return date before today, not returned)")
    void findAllByActualReturnDateIsNullAndReturnDateBefore_ReturnsOverdueRentals() {
        // Given
        LocalDate today = LocalDate.now();

        createRental(activeUser, testCar, today.minusDays(10), today.minusDays(2), null);

        createRental(anotherUser, testCar, today, today.plusDays(3), null);

        createRental(activeUser, testCar, today.minusDays(10), today.minusDays(2), today.minusDays(3));

        // When
        List<Rental> overdueRentals = rentalRepository.findAllByActualReturnDateIsNullAndReturnDateBefore(today);

        // Then
        assertEquals(1, overdueRentals.size());
        assertTrue(overdueRentals.get(0).getReturnDate().isBefore(today));
        assertNull(overdueRentals.get(0).getActualReturnDate());
    }

    @Test
    @DisplayName("Check if user has active rentals")
    void existsByUserIdAndActualReturnDateIsNull_ReturnsCorrectBoolean() {
        // Given
        createRental(activeUser, testCar, LocalDate.now(), null);
        // Given
        createRental(anotherUser, testCar, LocalDate.now().minusDays(5), LocalDate.now());

        // When & Then
        assertTrue(rentalRepository.existsByUserIdAndActualReturnDateIsNull(activeUser.getId()));
        assertFalse(rentalRepository.existsByUserIdAndActualReturnDateIsNull(anotherUser.getId()));
        assertFalse(rentalRepository.existsByUserIdAndActualReturnDateIsNull(999L));
    }


    private void createRental(User user, Car car, LocalDate rentalDate, LocalDate actualReturnDate) {
        createRental(user, car, rentalDate, rentalDate.plusDays(3), actualReturnDate);
    }

    private void createRental(User user, Car car, LocalDate rentalDate, LocalDate returnDate, LocalDate actualReturnDate) {
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(returnDate);
        rental.setActualReturnDate(actualReturnDate);
        entityManager.persist(rental);
    }
}
