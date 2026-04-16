package org.kucherenkoos.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kucherenkoos.carsharingservice.util.TestDataHelper.createTestCar;
import static org.kucherenkoos.carsharingservice.util.TestDataHelper.createTestUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User primaryUser;
    private User secondaryUser;
    private Rental primaryRental;

    @BeforeEach
    void setUp() {
        Car car = createTestCar();
        car.setId(null);
        car = entityManager.persist(car);

        primaryUser = createTestUser();
        primaryUser.setId(null);
        primaryUser.setPassword("password123");
        primaryUser.setFirstName("John");
        primaryUser.setLastName("Doe");
        primaryUser.setRoles(Collections.emptySet());
        primaryUser = entityManager.persist(primaryUser);

        secondaryUser = createTestUser();
        secondaryUser.setId(null);
        secondaryUser.setEmail("second@example.com");
        secondaryUser.setPassword("password123");
        secondaryUser.setFirstName("Jane");
        secondaryUser.setLastName("Smith");
        secondaryUser.setRoles(Collections.emptySet());
        secondaryUser = entityManager.persist(secondaryUser);

        primaryRental = new Rental();
        primaryRental.setCar(car);
        primaryRental.setUser(primaryUser);
        primaryRental.setRentalDate(LocalDate.now());
        primaryRental.setReturnDate(LocalDate.now().plusDays(2));
        primaryRental = entityManager.persist(primaryRental);

        Rental secondaryRental = new Rental();
        secondaryRental.setCar(car);
        secondaryRental.setUser(secondaryUser);
        secondaryRental.setRentalDate(LocalDate.now());
        secondaryRental.setReturnDate(LocalDate.now().plusDays(1));
        entityManager.persist(secondaryRental);
    }

    @Test
    @DisplayName("Find all payments by rental user ID")
    void findAllByRentalUserId_ReturnsPaymentsForSpecificUser() {
        // Given
        createPayment(primaryRental, "session_1", Payment.PaymentStatus.PENDING); // user 1
        createPayment(primaryRental, "session_2", Payment.PaymentStatus.PAID);    // user 1
        Rental anotherRental = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Rental r WHERE r.user.id = :userId", Rental.class)
                .setParameter("userId", secondaryUser.getId())
                .getSingleResult();
        createPayment(anotherRental, "session_3", Payment.PaymentStatus.PAID);    // user 2

        // When
        List<Payment> userPayments = paymentRepository.findAllByRentalUserId(primaryUser.getId());

        // Then
        assertEquals(2, userPayments.size());
        assertTrue(userPayments.stream()
                .allMatch(p -> p.getRental().getUser().getId().equals(primaryUser.getId())));
    }

    @Test
    @DisplayName("Find payment by Session ID")
    void findBySessionId_ValidSessionId_ReturnsPayment() {
        // Given
        String targetSessionId = "stripe_session_abc123";
        createPayment(primaryRental, targetSessionId, Payment.PaymentStatus.PENDING);
        createPayment(primaryRental, "other_session", Payment.PaymentStatus.PAID);

        // When
        Optional<Payment> result = paymentRepository.findBySessionId(targetSessionId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(targetSessionId, result.get().getSessionId());
    }

    @Test
    @DisplayName("Check if exists by user ID and status NOT matching")
    void existsByRentalUserIdAndStatusNot_ReturnsCorrectBoolean() {
        // Given
        createPayment(primaryRental, "session_1", Payment.PaymentStatus.PENDING);

        // When & Then
        assertTrue(paymentRepository.existsByRentalUserIdAndStatusNot(primaryUser.getId(), Payment.PaymentStatus.PAID));

        assertFalse(paymentRepository.existsByRentalUserIdAndStatusNot(primaryUser.getId(), Payment.PaymentStatus.PENDING));

        assertFalse(paymentRepository.existsByRentalUserIdAndStatusNot(999L, Payment.PaymentStatus.PAID));
    }

    @Test
    @DisplayName("Find all payments by a specific status")
    void findAllByStatus_ReturnsPaymentsMatchingStatus() {
        // Given
        createPayment(primaryRental, "session_1", Payment.PaymentStatus.PAID);
        createPayment(primaryRental, "session_2", Payment.PaymentStatus.PENDING);
        createPayment(primaryRental, "session_3", Payment.PaymentStatus.PAID);

        // When
        List<Payment> paidPayments = paymentRepository.findAllByStatus(Payment.PaymentStatus.PAID);

        // Then
        assertEquals(2, paidPayments.size());
        assertTrue(paidPayments.stream().allMatch(p -> p.getStatus() == Payment.PaymentStatus.PAID));
    }

    @Test
    @DisplayName("Find payment by rental ID and status")
    void findByRentalIdAndStatus_ReturnsMatchingPayment() {
        // Given
        createPayment(primaryRental, "session_1", Payment.PaymentStatus.PAID);

        // When
        Optional<Payment> result = paymentRepository.findByRentalIdAndStatus(primaryRental.getId(), Payment.PaymentStatus.PAID);
        Optional<Payment> emptyResult = paymentRepository.findByRentalIdAndStatus(primaryRental.getId(), Payment.PaymentStatus.PENDING);

        // Then
        assertTrue(result.isPresent());
        assertEquals(Payment.PaymentStatus.PAID, result.get().getStatus());
        assertEquals(primaryRental.getId(), result.get().getRental().getId());

        assertFalse(emptyResult.isPresent());
    }

    private void createPayment(Rental rental, String sessionId, Payment.PaymentStatus status) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setSessionId(sessionId);
        payment.setStatus(status);
        payment.setSessionUrl("https://example.com/checkout/" + sessionId);
        payment.setTotal(BigDecimal.valueOf(150.50));
        entityManager.persist(payment);
    }
}
