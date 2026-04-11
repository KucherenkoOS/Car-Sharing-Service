package org.kucherenkoos.carsharingservice.event;

import org.kucherenkoos.carsharingservice.model.Rental;

public record RentalCreatedEvent(Rental rental) {
}
