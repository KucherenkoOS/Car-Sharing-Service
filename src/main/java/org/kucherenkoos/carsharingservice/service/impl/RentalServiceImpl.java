package org.kucherenkoos.carsharingservice.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.event.RentalCreatedEvent;
import org.kucherenkoos.carsharingservice.exception.EntityNotFoundException;
import org.kucherenkoos.carsharingservice.mapper.RentalMapper;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.model.User;
import org.kucherenkoos.carsharingservice.repository.CarRepository;
import org.kucherenkoos.carsharingservice.repository.RentalRepository;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.kucherenkoos.carsharingservice.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final Logger LOGGER = LogManager.getLogger(RentalServiceImpl.class);
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RentalResponseDto createRental(CreateRentalRequestDto dto) {
        if (dto.getReturnDate().isBefore(dto.getRentalDate())) {
            throw new IllegalArgumentException("Return date cannot be before rental date");
        }

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> {
                    LOGGER.error("Failed to create rental. Car with ID: {} not found",
                            dto.getCarId());
                    return new EntityNotFoundException("Car not found: " + dto.getCarId());
                });

        if (car.getInventory() <= 0) {
            throw new IllegalStateException("Car is out of stock");
        }

        Rental rental = rentalMapper.toEntity(dto);
        rental.setCar(car);
        rental.setUser(userService.getCurrentUser());

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        Rental savedRental = rentalRepository.save(rental);
        eventPublisher.publishEvent(new RentalCreatedEvent(savedRental));

        return rentalMapper.toDto(savedRental);
    }

    @Override
    public List<Rental> findAllOverdue(LocalDate date) {
        return rentalRepository.findAllByActualReturnDateIsNullAndReturnDateBefore(date);
    }

    @Override
    public Page<RentalResponseDto> getRentals(Long userId, Boolean isActive, Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        Long searchUserId = isManager(currentUser) ? userId : currentUser.getId();

        return rentalRepository.findFilteredRentals(searchUserId, isActive, pageable)
                .map(rentalMapper::toDto);
    }

    @Override
    public RentalDetailDto getRentalById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Rental with ID: {} not found", id);
                    return new EntityNotFoundException("Rental not found: " + id);
                });

        User currentUser = userService.getCurrentUser();
        if (!isManager(currentUser) && !rental.getUser().getId().equals(currentUser.getId())) {
            LOGGER.warn(
                    "Security alert: User ID: {} "
                            + "attempted to access Rental ID: {} without permission",
                    currentUser.getId(), id);
            throw new AccessDeniedException("No access to this rental");
        }

        return rentalMapper.toDetailDto(rental);
    }

    @Override
    @Transactional
    public RentalResponseDto returnRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Failed to return rental. Rental with ID: {} not found", id);
                    return new EntityNotFoundException("Rental not found: " + id);
                });

        if (rental.getActualReturnDate() != null) {
            throw new IllegalStateException("Rental was already returned");
        }

        rental.setActualReturnDate(LocalDate.now());

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    private boolean isManager(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_MANAGER"));
    }
}
