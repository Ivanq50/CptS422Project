package org.cpts422.carrentalapp.repo;

import org.cpts422.carrentalapp.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.cpts422.carrentalapp.model.AppUser;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserAndReturnDateIsNull(AppUser user);

    List<Rental> findByUserId(Long userId);
}