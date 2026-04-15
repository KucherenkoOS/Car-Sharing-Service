package org.kucherenkoos.carsharingservice.event.payment;

import org.kucherenkoos.carsharingservice.model.Payment;

public record PaymentSuccessfulEvent(Payment payment) {
}
