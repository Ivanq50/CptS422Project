package org.cpts422.carrentalapp.repo;

import org.cpts422.carrentalapp.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserIdAndReturnedAtIsNull(Long userId);
    long countByUserIdAndReturnedAtIsNull(Long userId);
    boolean existsByUserIdAndReturnedAtIsNull(Long userId);
    Optional<Rental> findFirstByUserIdAndReturnedAtIsNull(Long userId);
    List<Rental> findByUserIdOrderByRentedAtDesc(Long userId);
}