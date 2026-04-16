package org.kucherenkoos.carsharingservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kucherenkoos.carsharingservice.exception.TelegramNotificationException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

    private final String botToken = "test_bot_token";
    private final String chatId = "123456789";
    private final String expectedUrl = "https://api.telegram.org/bottest_bot_token/sendMessage";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(telegramNotificationService, "botToken", botToken);
        ReflectionTestUtils.setField(telegramNotificationService, "chatId", chatId);
    }

    @Test
    @DisplayName("Send notification: Success")
    void sendNotification_ValidMessage_SuccessfullySendsRequest() {
        // Given
        String message = "Hello, this is a test notification!";

        // When
        telegramNotificationService.sendNotification(message);

        // Then
        ArgumentCaptor<Object> requestCaptor = ArgumentCaptor.forClass(Object.class);

        verify(restTemplate, times(1)).postForObject(
                eq(expectedUrl),
                requestCaptor.capture(),
                eq(String.class)
        );

        Object capturedRequest = requestCaptor.getValue();

        assertEquals("https://api.telegram.org/bottest_bot_token/sendMessage", expectedUrl);
    }

    @Test
    @DisplayName("Send notification: Throws TelegramNotificationException on RestTemplate error")
    void sendNotification_ApiThrowsException_ThrowsTelegramNotificationException() {
        // Given
        String message = "This message will fail";

        when(restTemplate.postForObject(eq(expectedUrl), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection timed out"));

        // When & Then
        TelegramNotificationException exception = assertThrows(
                TelegramNotificationException.class,
                () -> telegramNotificationService.sendNotification(message)
        );

        assertEquals("Failed to send Telegram notification. Message: Connection timed out",
                exception.getMessage());
    }
}
