package org.kucherenkoos.carsharingservice.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.kucherenkoos.carsharingservice.repository.PaymentRepository;
import org.kucherenkoos.carsharingservice.service.impl.StripeService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpiredPaymentSchedulerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private ExpiredPaymentScheduler scheduler;

    private Payment payment;
    private Session stripeSession;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setSessionId("session_123");
        payment.setStatus(Payment.PaymentStatus.PENDING);

        stripeSession = mock(Session.class);
    }

    @Test
    @DisplayName("Should return early if no pending payments found")
    void checkStripeSessionsStatus_NoPendingPayments_DoesNothing() {
        // Given
        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // When
        scheduler.checkStripeSessionsStatus();

        // Then
        verifyNoInteractions(stripeService);
        verify(paymentRepository, times(1)).findAllByStatus(Payment.PaymentStatus.PENDING);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark as PAID if session is complete and paid")
    void checkStripeSessionsStatus_SessionCompleteAndPaid_UpdatesToPaid() throws Exception {
        // Given
        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(payment));
        when(stripeService.getSession("session_123")).thenReturn(stripeSession);

        when(stripeSession.getStatus()).thenReturn("complete");
        when(stripeSession.getPaymentStatus()).thenReturn("paid");

        // When
        scheduler.checkStripeSessionsStatus();

        // Then
        assertEquals(Payment.PaymentStatus.PAID, payment.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Should mark as EXPIRED if session is expired without PaymentIntent")
    void checkStripeSessionsStatus_SessionExpiredNoIntent_UpdatesToExpired() throws Exception {
        // Given
        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(payment));
        when(stripeService.getSession("session_123")).thenReturn(stripeSession);

        when(stripeSession.getStatus()).thenReturn("expired");
        when(stripeSession.getPaymentIntent()).thenReturn(null);

        // When
        scheduler.checkStripeSessionsStatus();

        // Then
        assertEquals(Payment.PaymentStatus.EXPIRED, payment.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Should mark as FAILED if intent status is requires_payment_method")
    void checkStripeSessionsStatus_SessionExpiredWithRequiresPaymentMethod_UpdatesToFailed() throws Exception {
        // Given
        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(payment));
        when(stripeService.getSession("session_123")).thenReturn(stripeSession);

        when(stripeSession.getStatus()).thenReturn("expired");
        when(stripeSession.getPaymentIntent()).thenReturn("pi_123");

        PaymentIntent mockedIntent = mock(PaymentIntent.class);
        when(mockedIntent.getStatus()).thenReturn("requires_payment_method");

        try (MockedStatic<PaymentIntent> mockedStaticIntent = mockStatic(PaymentIntent.class)) {
            mockedStaticIntent.when(() -> PaymentIntent.retrieve("pi_123")).thenReturn(mockedIntent);

            // When
            scheduler.checkStripeSessionsStatus();
        }

        // Then
        assertEquals(Payment.PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Should catch Exception, log it, and process remaining payments")
    void checkStripeSessionsStatus_StripeThrowsException_LogsAndContinues() throws Exception {
        Payment failedPayment = new Payment();
        failedPayment.setSessionId("session_error");
        failedPayment.setStatus(Payment.PaymentStatus.PENDING);

        Payment successPayment = new Payment();
        successPayment.setSessionId("session_success");
        successPayment.setStatus(Payment.PaymentStatus.PENDING);

        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(failedPayment, successPayment));

        when(stripeService.getSession("session_error"))
                .thenThrow(new RuntimeException("Stripe API connection timeout"));

        Session successSession = mock(Session.class);
        when(stripeService.getSession("session_success")).thenReturn(successSession);
        when(successSession.getStatus()).thenReturn("complete");
        when(successSession.getPaymentStatus()).thenReturn("paid");

        // When
        scheduler.checkStripeSessionsStatus();

        // Then
        assertEquals(Payment.PaymentStatus.PENDING, failedPayment.getStatus());

        assertEquals(Payment.PaymentStatus.PAID, successPayment.getStatus());
        verify(paymentRepository, times(1)).save(successPayment);
        verify(paymentRepository, never()).save(failedPayment);
    }
}
