package org.kucherenkoos.carsharingservice.mapper;

import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental rental);

    RentalDetailDto toDetailDto(Rental rental);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    Rental toEntity(CreateRentalRequestDto dto);
}
