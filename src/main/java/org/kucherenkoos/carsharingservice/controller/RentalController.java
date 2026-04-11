package org.kucherenkoos.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Tag(name = "Rentals management", description = "Endpoints for managing rentals")
public class RentalController {
    private final RentalService rentalService;

    @Operation(summary = "Create a new rental")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponseDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.createRental(requestDto);
    }

    @Operation(summary = "List all rentals with pagination and sorting")
    @GetMapping
    public Page<RentalResponseDto> getRentals(
            @Parameter(description
                    = "Specific User ID available for managers only")
            @RequestParam(name = "user_id", required = false) Long userId,
            @Parameter(description
                    = "Filter by active status (true - not returned, false - returned)")
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            Pageable pageable) {
        return rentalService.getRentals(userId, isActive, pageable);
    }

    @Operation(summary = "Watch details about specific rental")
    @GetMapping("/{id}")
    public RentalDetailDto getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @Operation(summary = "Return rental")
    @PostMapping("/{id}/return")
    public RentalResponseDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }

}
