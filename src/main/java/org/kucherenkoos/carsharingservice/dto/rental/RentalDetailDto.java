package org.kucherenkoos.carsharingservice.dto.rental;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;

@Getter
@Setter
@ToString
public class RentalDetailDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarDto car;
}
