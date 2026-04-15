package org.kucherenkoos.carsharingservice.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request DTO for payments")
public class PaymentRequestDto {

    @Schema(description = "Rental ID", example = "1")
    private Long rentalId;
}
