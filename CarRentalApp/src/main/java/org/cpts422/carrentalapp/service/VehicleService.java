package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.error.VehicleNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {
    private final VehicleRepository vehicles;

    public VehicleService(VehicleRepository vehicles) { this.vehicles = vehicles; }

    public List<Vehicle> list(Boolean available) {
        return (available == null) ? vehicles.findAll() : vehicles.findByAvailable(available);
    }

    public Vehicle get(Long id) {
        return vehicles.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
    }
}
