package org.kucherenkoos.carsharingservice.scheduler;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.kucherenkoos.carsharingservice.repository.PaymentRepository;
import org.kucherenkoos.carsharingservice.service.impl.StripeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ExpiredPaymentScheduler {
    private static final Logger LOGGER = LogManager.getLogger(ExpiredPaymentScheduler.class);
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkStripeSessionsStatus() {
        List<Payment> pendingPayments =
                paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING);

        if (pendingPayments.isEmpty()) {
            return;
        }

        for (Payment payment : pendingPayments) {
            try {
                Session session = stripeService.getSession(payment.getSessionId());

                if ("expired".equals(session.getStatus())) {
                    processExpiredSession(payment, session);
                } else if ("complete".equals(session.getStatus())
                        && "paid".equals(session.getPaymentStatus())) {
                    payment.setStatus(Payment.PaymentStatus.PAID);
                    paymentRepository.save(payment);
                    LOGGER.info("Payment session {} marked as PAID by scheduler",
                            payment.getSessionId());
                }
            } catch (Exception e) {
                LOGGER.error("Error syncing status for session {}: {}",
                        payment.getSessionId(), e.getMessage(), e);
            }
        }
    }

    private void processExpiredSession(Payment payment, Session session) throws Exception {
        if (session.getPaymentIntent() != null) {
            PaymentIntent intent = PaymentIntent.retrieve(session.getPaymentIntent());

            if ("requires_payment_method".equals(intent.getStatus())) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                LOGGER.info("Payment session {} marked as FAILED (card declined/rejected)",
                        payment.getSessionId());
                return;
            }
        }

        payment.setStatus(Payment.PaymentStatus.EXPIRED);
        paymentRepository.save(payment);
        LOGGER.info("Payment session {} marked as EXPIRED in DB", payment.getSessionId());
    }
}
