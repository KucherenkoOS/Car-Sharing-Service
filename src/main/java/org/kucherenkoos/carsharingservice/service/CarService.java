package org.kucherenkoos.carsharingservice.service;

import org.kucherenkoos.carsharingservice.dto.car.CarDto;
import org.kucherenkoos.carsharingservice.dto.car.CreateCarRequestDto;
import org.kucherenkoos.carsharingservice.dto.car.UpdateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto createCar(CreateCarRequestDto carDto);

    CarDto getCarById(Long id);

    Page<CarDto> getAll(Pageable pageable);

    CarDto update(Long id, UpdateCarRequestDto carDto);

    void deleteById(Long id);
}
