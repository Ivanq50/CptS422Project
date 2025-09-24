package org.cpts422.carrentalapp.repo;

import org.cpts422.carrentalapp.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserAndReturnDateIsNull(Long userId);
    List<Rental> findByUserId(Long userId);
}