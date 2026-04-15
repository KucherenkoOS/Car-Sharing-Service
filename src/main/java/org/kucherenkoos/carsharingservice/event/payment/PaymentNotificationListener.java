package org.kucherenkoos.carsharingservice.event.payment;

import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.kucherenkoos.carsharingservice.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentNotificationListener {
    private final NotificationService notificationService;

    @EventListener
    public void handlePaymentSuccessful(PaymentSuccessfulEvent event) {
        Payment payment = event.payment();

        String message = String.format(
                "💰 *Payment Successful!*\n\n"
                        + "*Payment ID:* %d\n"
                        + "*Rental ID:* %d\n"
                        + "*Amount:* $%.2f\n"
                        + "*Type:* %s\n"
                        + "*User:* %s\n",
                payment.getId(),
                payment.getRental().getId(),
                payment.getTotal(),
                payment.getType(),
                payment.getRental().getUser().getEmail()
        );

        notificationService.sendNotification(message);
    }
}
