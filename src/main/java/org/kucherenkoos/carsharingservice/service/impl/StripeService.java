package org.kucherenkoos.carsharingservice.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kucherenkoos.carsharingservice.exception.StripeSessionException;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class StripeService {
    private static final Logger LOGGER = LogManager.getLogger(StripeService.class);

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    public Session createStripeSession(BigDecimal amount, Payment.PaymentType type, Rental rental) {
        Stripe.apiKey = secretKey;

        long unitAmount = amount.multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(UriComponentsBuilder.fromUriString(successUrl)
                        .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                        .build(false)
                        .toUriString())
                .setCancelUrl(UriComponentsBuilder.fromUriString(cancelUrl)
                        .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                        .build(false)
                        .toUriString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Rental payment for car: "
                                                + rental.getCar().getBrand()
                                                + " "
                                                + rental.getCar().getModel())
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            return Session.create(params);
        } catch (StripeException e) {
            LOGGER.error("Stripe session creation failed", e);
            throw new StripeSessionException("Stripe session creation failed");
        }
    }

    public Session getSession(String sessionId) {
        try {
            Stripe.apiKey = secretKey;
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            LOGGER.error("Stripe session retrieval failed with session id {}",
                    sessionId, e);
            throw new StripeSessionException("Could not retrieve Stripe session: "
                    + sessionId);
        }
    }
}
