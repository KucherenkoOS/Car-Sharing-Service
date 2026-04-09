package org.kucherenkoos.carsharingservice.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponseDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.createRental(requestDto);
    }

    @GetMapping
    public List<RentalResponseDto> getRentals(
            @RequestParam(name = "user_id", required = false) Long userId,
            @RequestParam(name = "is_active", required = false) Boolean isActive) {
        return rentalService.getRentals(userId, isActive);
    }

    @GetMapping("/{id}")
    public RentalDetailDto getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @PostMapping("/{id}/return")
    public RentalResponseDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }

}
