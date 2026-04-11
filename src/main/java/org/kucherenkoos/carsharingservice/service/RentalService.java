package org.kucherenkoos.carsharingservice.service;

import java.time.LocalDate;
import java.util.List;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto createRental(CreateRentalRequestDto requestDto);

    List<Rental> findAllOverdue(LocalDate date);

    Page<RentalResponseDto> getRentals(Long userId, Boolean isActive, Pageable pageable);

    RentalDetailDto getRentalById(Long id);

    RentalResponseDto returnRental(Long id);
}
