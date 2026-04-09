package org.kucherenkoos.carsharingservice.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.exception.EntityNotFoundException;
import org.kucherenkoos.carsharingservice.mapper.RentalMapper;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.kucherenkoos.carsharingservice.model.User;
import org.kucherenkoos.carsharingservice.repository.CarRepository;
import org.kucherenkoos.carsharingservice.repository.RentalRepository;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.kucherenkoos.carsharingservice.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final UserService userService;

    @Override
    @Transactional
    public RentalResponseDto createRental(CreateRentalRequestDto dto) {
        if (dto.getReturnDate().isBefore(dto.getRentalDate())) {
            throw new IllegalArgumentException("Return date cannot be before rental date");
        }

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + dto.getCarId()));

        if (car.getInventory() <= 0) {
            throw new IllegalStateException("Car is out of stock");
        }

        Rental rental = rentalMapper.toEntity(dto);
        rental.setCar(car);
        rental.setUser(userService.getCurrentUser());

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public List<RentalResponseDto> getRentals(Long userId, Boolean isActive) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = isManager(currentUser);

        List<Rental> rentals;
        if (isAdmin) {
            rentals =
                    (userId != null)
                            ? rentalRepository.findByUserId(userId)
                            : rentalRepository.findAll();
        } else {
            rentals = rentalRepository.findByUserId(currentUser.getId());
        }

        if (isActive != null) {
            rentals = rentals.stream()
                    .filter(r -> isActive
                            ? r.getActualReturnDate() == null
                            : r.getActualReturnDate() != null)
                    .toList();
        }

        return rentals.stream().map(rentalMapper::toDto).toList();
    }

    @Override
    public RentalDetailDto getRentalById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found: " + id));

        User currentUser = userService.getCurrentUser();
        if (!isManager(currentUser) && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No access to this rental");
        }

        return rentalMapper.toDetailDto(rental);
    }

    @Override
    @Transactional
    public RentalResponseDto returnRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found: " + id));

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
