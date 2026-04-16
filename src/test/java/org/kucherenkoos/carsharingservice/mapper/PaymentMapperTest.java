package org.kucherenkoos.carsharingservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.dto.payment.PaymentResponseDto;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.kucherenkoos.carsharingservice.model.Rental;

class PaymentMapperTest {

    private final PaymentMapper paymentMapper = new PaymentMapperImpl();

    @Test
    @DisplayName("Map Payment entity to PaymentResponseDto (extracts rentalId)")
    void toDto_ValidPayment_ReturnsDtoWithRentalId() {
        // Given
        Rental rental = new Rental();
        rental.setId(99L);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setRental(rental);

        payment.setSessionUrl("https://checkout.stripe.com/session_123");
        payment.setSessionId("session_123");
        payment.setTotal(BigDecimal.valueOf(50.00));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setType(Payment.PaymentType.PAYMENT);

        // When
        PaymentResponseDto dto = paymentMapper.toDto(payment);

        // Then
        assertNotNull(dto);
        assertEquals(payment.getId(), dto.getId());
        assertEquals(payment.getSessionUrl(), dto.getSessionUrl());
        assertEquals(payment.getSessionId(), dto.getSessionId());
        assertEquals(payment.getTotal(), dto.getTotal());

        assertEquals(99L, dto.getRentalId(), "Rental ID from entity");
    }
}
