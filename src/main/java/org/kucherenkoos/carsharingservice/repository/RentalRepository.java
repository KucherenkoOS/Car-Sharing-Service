package org.kucherenkoos.carsharingservice.repository;

import java.time.LocalDate;
import java.util.List;
import org.kucherenkoos.carsharingservice.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query("SELECT r FROM Rental r WHERE "
            + "(:userId IS NULL OR r.user.id = :userId) AND "
            + "(:isActive IS NULL OR "
            + "(:isActive = true AND r.actualReturnDate IS NULL) OR "
            + "(:isActive = false AND r.actualReturnDate IS NOT NULL))")
    Page<Rental> findFilteredRentals(
            @Param("userId") Long userId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT r FROM Rental r "
            + "JOIN FETCH r.user "
            + "JOIN FETCH r.car "
            + "WHERE r.actualReturnDate IS NULL AND r.returnDate < :date")
    List<Rental> findAllByActualReturnDateIsNullAndReturnDateBefore(@Param("date") LocalDate date);
}
