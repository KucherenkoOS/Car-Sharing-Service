package org.kucherenkoos.carsharingservice.service;

import java.util.List;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;

public interface RentalService {
    RentalResponseDto createRental(CreateRentalRequestDto requestDto);

    List<RentalResponseDto> getRentals(Long userId, Boolean isActive);

    RentalDetailDto getRentalById(Long id);

    RentalResponseDto returnRental(Long id);
}
