package org.kucherenkoos.carsharingservice.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;

@Getter
@Setter
@ToString
@Schema(description = "Response DTO for viewing info about rental")
public class RentalDetailDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarDto car;
}
