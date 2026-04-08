package org.kucherenkoos.carsharingservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;
import org.kucherenkoos.carsharingservice.dto.car.CreateCarRequestDto;
import org.kucherenkoos.carsharingservice.dto.car.UpdateCarRequestDto;
import org.kucherenkoos.carsharingservice.exception.EntityNotFoundException;
import org.kucherenkoos.carsharingservice.mapper.CarMapper;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.repository.CarRepository;
import org.kucherenkoos.carsharingservice.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto createCar(CreateCarRequestDto requestDto) {
        Car car = carMapper.toEntity(requestDto);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public CarDto getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find car by id: " + id));

        return carMapper.toDto(car);
    }

    @Override
    public Page<CarDto> getAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Transactional
    @Override
    public CarDto update(Long id, UpdateCarRequestDto requestDto) {
        Car car = carRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find car by id: " + id));

        carMapper.updateCarFromDto(requestDto, car);

        return carMapper.toDto(car);
    }

    @Override
    public void deleteById(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find car by id: " + id);
        }
        carRepository.deleteById(id);
    }
}
