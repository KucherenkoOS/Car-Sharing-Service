package org.kucherenkoos.carsharingservice.repository;

import org.kucherenkoos.carsharingservice.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car,Long> {
}
