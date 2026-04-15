package org.kucherenkoos.carsharingservice.event.rental;

import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalNotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleRentalCreated(RentalCreatedEvent event) {
        Rental rental = event.rental();
        String message = String.format(
                "🚀 *New Rental Created!*\n\n"
                        + "*Car:* %s %s\n"
                        + "*User Email:* %s\n"
                        + "*First name:* %s\n"
                        + "*Last name:* %s\n"
                        + "*User ID:* %s\n"
                        + "*Return Date:* %s",
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getUser().getEmail(),
                rental.getUser().getFirstName(),
                rental.getUser().getLastName(),
                rental.getUser().getId(),
                rental.getReturnDate()
        );
        notificationService.sendNotification(message);
    }
}
