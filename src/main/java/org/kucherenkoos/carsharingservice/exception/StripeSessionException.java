package org.kucherenkoos.carsharingservice.exception;

public class StripeSessionException extends RuntimeException {
    public StripeSessionException(String message) {
        super(message);
    }
}
