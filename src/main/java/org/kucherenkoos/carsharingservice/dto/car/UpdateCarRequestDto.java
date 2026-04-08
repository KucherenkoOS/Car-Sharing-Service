package org.kucherenkoos.carsharingservice.dto.car;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kucherenkoos.carsharingservice.model.CarType;

@Getter
@Setter
@ToString
@Schema(description = "Update DTO for car")
public class UpdateCarRequestDto {

    @Schema(description = "Car model", example = "Juke")
    @Size(min = 1)
    @Pattern(regexp = ".*\\S.*", message = "Must not be blank")
    private String model;

    @Schema(description = "Car brand", example = "Nissan")
    @Size(min = 1)
    @Pattern(regexp = ".*\\S.*", message = "Must not be blank")
    private String brand;

    @Schema(description = "Enum of car type", example = "SUV")
    private CarType carType;

    @Schema(description = "Number of available cars", example = "2")
    @Min(0)
    private Integer inventory;

    @Schema(description = "Daily fee for a car", example = "49.99")
    @DecimalMin("0.0")
    private BigDecimal dailyFee;
}
