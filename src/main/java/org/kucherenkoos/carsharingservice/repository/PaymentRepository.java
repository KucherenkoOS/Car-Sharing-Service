package org.kucherenkoos.carsharingservice.repository;

import java.util.List;
import java.util.Optional;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    List<Payment> findAllByRentalUserId(Long userId);

    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByRentalUserIdAndStatusNot(Long userId, Payment.PaymentStatus status);

    List<Payment> findAllByStatus(Payment.PaymentStatus status);

    Optional<Payment> findByRentalIdAndStatus(Long rentalId, Payment.PaymentStatus status);
}
