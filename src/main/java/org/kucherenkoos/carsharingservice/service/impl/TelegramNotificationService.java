package org.kucherenkoos.carsharingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kucherenkoos.carsharingservice.exception.TelegramNotificationException;
import org.kucherenkoos.carsharingservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot{token}/sendMessage";
    private static final Logger LOGGER = LogManager.getLogger(TelegramNotificationService.class);
    private final RestTemplate restTemplate = new RestTemplate();

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

    private record SendMessageRequest(String chat_id, String text, String parse_mode) {}
}
