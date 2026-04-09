package org.kucherenkoos.carsharingservice.dto.rental;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateRentalRequestDto {
    @NotNull(message = "carId is required")
    private Long carId;

    @NotNull(message = "rentalDate is required")
    @FutureOrPresent(message = "rentalDate cannot be in the past")
    private LocalDate rentalDate;

    @NotNull(message = "returnDate is required")
    private LocalDate returnDate;
}
