package org.kucherenkoos.carsharingservice.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kucherenkoos.carsharingservice.event.RentalCreatedEvent;
import org.kucherenkoos.carsharingservice.exception.TelegramNotificationException;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.service.NotificationService;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot{token}/sendMessage";
    private static final Logger LOGGER = LogManager.getLogger(TelegramNotificationService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final RentalService rentalService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    @Override
    public void sendNotification(String message) {
        String url = TELEGRAM_API_URL.replace("{token}", botToken);

        SendMessageRequest request = new SendMessageRequest(chatId, message, "Markdown");

        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            LOGGER.error("Failed to send Telegram notification. Message: {}",
                    e.getMessage(), e);
            throw new TelegramNotificationException(
                    "Failed to send Telegram notification. Message: "
                    + e.getMessage());
        }
    }

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
        sendNotification(message);
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void notifyOverdueRentals() {
        List<Rental> overdueRentals = rentalService.findAllOverdue(LocalDate.now());

        if (overdueRentals.isEmpty()) {
            sendNotification("✅ *No overdue rentals today!*");
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
        sendNotification(message.toString());
    }

    private record SendMessageRequest(String chat_id, String text, String parse_mode) {}
}
