package org.kucherenkoos.carsharingservice.mapper;

import org.kucherenkoos.carsharingservice.dto.payment.PaymentResponseDto;
import org.kucherenkoos.carsharingservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "rental.id", target = "rentalId")
    PaymentResponseDto toDto(Payment payment);
}
