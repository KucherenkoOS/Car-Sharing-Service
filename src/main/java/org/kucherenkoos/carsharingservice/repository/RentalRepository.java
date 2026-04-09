package org.kucherenkoos.carsharingservice.repository;

import java.util.List;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental,Long> {
    List<Rental> findByUserId(Long userId);
}
