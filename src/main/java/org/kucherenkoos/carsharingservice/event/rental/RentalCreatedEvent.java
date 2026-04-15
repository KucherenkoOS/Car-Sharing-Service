package org.kucherenkoos.carsharingservice.event.rental;

import org.kucherenkoos.carsharingservice.model.Rental;

public record RentalCreatedEvent(Rental rental) {
}
