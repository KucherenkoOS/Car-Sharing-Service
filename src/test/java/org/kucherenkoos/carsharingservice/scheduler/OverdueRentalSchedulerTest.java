package org.kucherenkoos.carsharingservice.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.model.User;
import org.kucherenkoos.carsharingservice.service.NotificationService;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OverdueRentalSchedulerTest {

    @Mock
    private RentalService rentalService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OverdueRentalScheduler scheduler;

    @Test
    @DisplayName("Should send 'No overdue rentals' message when list is empty")
    void notifyOverdueRentals_EmptyList_SendsSuccessMessage() {
        // Given
        when(rentalService.findAllOverdue(any(LocalDate.class))).thenReturn(Collections.emptyList());

        // When
        scheduler.notifyOverdueRentals();

        // Then
        verify(notificationService, times(1)).sendNotification("✅ *No overdue rentals today!*");
    }

    @Test
    @DisplayName("Should send formatted message when overdue rentals exist")
    void notifyOverdueRentals_WithOverdueRentals_SendsFormattedMessage() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");

        Car car = new Car();
        car.setModel("Model S");
        car.setBrand("Tesla");

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setReturnDate(LocalDate.of(2026, 4, 10));

        when(rentalService.findAllOverdue(any(LocalDate.class))).thenReturn(List.of(rental));

        // When
        scheduler.notifyOverdueRentals();

        // Then
        String expectedMessage = "⚠️ *Overdue Rentals Found:*\n\n"
                + "• *User:* test@example.com, *Car:* Model S Tesla(Due: 2026-04-10)\n";

        verify(notificationService, times(1)).sendNotification(expectedMessage);
    }
}
