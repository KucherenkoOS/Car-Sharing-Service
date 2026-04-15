package org.kucherenkoos.carsharingservice.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kucherenkoos.carsharingservice.model.Payment;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO with payment info")
public class PaymentResponseDto {
    private Long id;
    private Long rentalId;
    private BigDecimal total;
    private Payment.PaymentStatus status;
    private Payment.PaymentType type;
    private String sessionUrl;
    private String sessionId;
}
