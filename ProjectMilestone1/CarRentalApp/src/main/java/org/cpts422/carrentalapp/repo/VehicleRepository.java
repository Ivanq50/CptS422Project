package org.cpts422.carrentalapp.repo;

import org.cpts422.carrentalapp.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByAvailable(boolean available);
}