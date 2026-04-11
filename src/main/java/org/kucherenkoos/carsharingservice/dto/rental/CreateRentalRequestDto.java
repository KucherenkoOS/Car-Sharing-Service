package org.kucherenkoos.carsharingservice.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Request DTO for creating rental")
public class CreateRentalRequestDto {
    @Schema(description = "Car id", example = "2")
    @NotNull(message = "carId is required")
    private Long carId;

    @Schema(description = "Rental date from", example = "2026-06-10")
    @NotNull(message = "rentalDate is required")
    @FutureOrPresent(message = "rentalDate cannot be in the past")
    private LocalDate rentalDate;

    @Schema(description = "Rental date to", example = "2026-07-01")
    @NotNull(message = "returnDate is required")
    private LocalDate returnDate;
}
