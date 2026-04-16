package org.kucherenkoos.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;
import org.kucherenkoos.carsharingservice.dto.car.CreateCarRequestDto;
import org.kucherenkoos.carsharingservice.dto.car.UpdateCarRequestDto;
import org.kucherenkoos.carsharingservice.service.CarService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Tag(name = "Cars management", description = "Endpoints for managing cars")
public class CarController {

    private final CarService carService;

    @Operation(summary = "Get all cars with pagination and sorting")
    @GetMapping
    public Page<CarDto> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "model") Pageable pageable) {
        return carService.getAll(pageable);
    }

    @Operation(summary = "Get a car by it's id")
    @GetMapping("/{id}")
    public CarDto findById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @Operation(summary = "Create a car (Manager operation)")
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public CarDto create(@RequestBody @Valid CreateCarRequestDto dto) {
        return carService.createCar(dto);
    }

    @Operation(summary = "Update an existing car by id (Manager operation)")
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}")
    public CarDto update(@PathVariable Long id,
                         @RequestBody @Valid UpdateCarRequestDto dto) {
        return carService.update(id, dto);
    }

    @Operation(summary = "Delete car by id (soft delete)")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
