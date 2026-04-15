package org.kucherenkoos.carsharingservice.scheduler;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.service.NotificationService;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverdueRentalScheduler {

    private final RentalService rentalService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void notifyOverdueRentals() {
        List<Rental> overdueRentals = rentalService.findAllOverdue(LocalDate.now());

        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification("✅ *No overdue rentals today!*");
            return;
        }

        StringBuilder message = new StringBuilder("⚠️ *Overdue Rentals Found:*\n\n");
        for (Rental rental : overdueRentals) {
            message.append(String.format(
                    "• *User:* %s, *Car:* %s %s(Due: %s)\n",
                    rental.getUser().getEmail(),
                    rental.getCar().getModel(),
                    rental.getCar().getBrand(),
                    rental.getReturnDate()
            ));
        }
        notificationService.sendNotification(message.toString());
    }
}
